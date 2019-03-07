/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.tree

import scala.annotation.tailrec

/**
 * Generic rose tree.
 */
abstract class Tree[A] {
  import Tree.Forest

  def label: A
  def children: Forest[A]

  /**
   * Map this tree into a new tree using f.
   */
  def map[B](f: A => B): Tree[B] = {
    Tree(f(label), children map (_ map f))
  }

  /**
   * Create a new location focused on this tree.
   */
  def location: Tree.Location[A] = Tree.Location(this, Nil, Nil, Nil)

  /**
   * Print the tree (in markdown format).
   */
  def show: String = {
    @tailrec def showNodes(loc: Tree.Location[A], result: List[String]): List[String] = {
      val lines = result ::: indent(loc.depth, "- ", "  ", loc.tree.label.toString.split('\n').toList)
      loc.next match {
        case Some(next) => showNodes(next, lines)
        case _          => lines
      }
    }
    def indent(depth: Int, first: String, other: String, lines: List[String]): List[String] = {
      val spaces = "  " * depth
      ((spaces + first) :: List.fill(lines.size - 1)(spaces + other)).zip(lines) map { case (i, l) => i + l }
    }
    showNodes(location, Nil).mkString("\n")
  }
}

object Tree {

  type Forest[A] = List[Tree[A]]

  /**
   * Default Tree implementation.
   */
  case class Node[A](label: A, children: Forest[A]) extends Tree[A] {
    override def toString = s"Node($label)"
  }

  /**
   * Create a new tree node (default Node).
   */
  def apply[A](label: A, children: Forest[A]): Tree[A] = Node(label, children)

  /**
   * Create a new tree node (with varargs for children).
   */
  def node[A](label: A, children: Tree[A]*): Tree[A] = Tree(label, children.toList)

  /**
   * Create a new tree node without children.
   */
  def leaf[A](label: A): Tree[A] = Tree(label, Nil)

  /**
   * Parent node for zipper locations.
   */
  final case class Parent[A](label: A, lefts: Forest[A], rights: Forest[A])

  /**
   * Zipper navigation and modification for Tree.
   */
  final case class Location[A](tree: Tree[A], lefts: Forest[A], rights: Forest[A], parents: List[Parent[A]]) {

    /**
     * Move to the root.
     */
    @tailrec
    def root: Location[A] = parent match {
      case Some(parent) => parent.root
      case None         => this
    }

    /**
     * Move to the parent.
     */
    def parent: Option[Location[A]] = parents match {
      case p :: ps => Some(Location(Tree(p.label, forest), p.lefts, p.rights, ps))
      case Nil     => None
    }

    /**
     * This node and its siblings.
     */
    def forest: Forest[A] = {
      lefts.reverse ::: tree :: rights
    }

    /**
     * Move to the left sibling.
     */
    def left: Option[Location[A]] = lefts match {
      case l :: ls => Some(Location(l, ls, tree :: rights, parents))
      case Nil     => None
    }

    /**
     * Move to the right sibling.
     */
    def right: Option[Location[A]] = rights match {
      case r :: rs => Some(Location(r, tree :: lefts, rs, parents))
      case Nil     => None
    }

    /**
     * Move to the leftmost child.
     */
    def leftmostChild: Option[Location[A]] = tree.children match {
      case t :: ts => Some(Location(t, Nil, ts, descend))
      case Nil     => None
    }

    /**
     * Move to the rightmost child.
     */
    def rightmostChild: Option[Location[A]] = tree.children.reverse match {
      case t :: ts => Some(Location(t, ts, Nil, descend))
      case Nil     => None
    }

    /**
     * Move to the nth child.
     */
    def child(n: Int): Option[Location[A]] = tree.children.splitAt(n) match {
      case (ls, t :: rs) => Some(Location(t, ls.reverse, rs, descend))
      case _             => None
    }

    /**
     * This node as a parent, before its parents.
     */
    private def descend: List[Parent[A]] = {
      Parent(tree.label, lefts, rights) :: parents
    }

    /**
     * Move to the next location in the hierarchy in depth-first order.
     */
    def next: Option[Location[A]] = {
      leftmostChild orElse nextRight
    }

    /**
     * Move to the right, otherwise the next possible right in a parent.
     */
    @tailrec
    def nextRight: Option[Location[A]] = {
      right match {
        case None => parent match {
          case Some(parent) => parent.nextRight
          case None         => None
        }
        case right => right
      }
    }

    /**
     * Move to the previous location in the hierarchy in depth-first order.
     */
    def prev: Option[Location[A]] = {
      left match {
        case Some(left) => left.deepRight
        case None       => parent
      }
    }

    /**
     * Move to the deepest rightmost node.
     */
    @tailrec
    def deepRight: Option[Location[A]] = {
      rightmostChild match {
        case Some(child) => child.deepRight
        case None        => Some(this)
      }
    }

    /**
     * Path from here up to the root.
     */
    def path: List[Location[A]] = this :: parent.toList.flatMap(_.path)

    /**
     * Depth of location from the root.
     */
    def depth: Int = parents.size

    /**
     * Is this the root node?
     */
    def isRoot: Boolean = parent.isEmpty

    /**
     * Is this a child node?
     */
    def isChild: Boolean = !isRoot

    /**
     * Is this a leaf node (no children)?
     */
    def isLeaf: Boolean = tree.children.isEmpty

    /**
     * Is this a branch node (has children)?
     */
    def isBranch: Boolean = !isLeaf

    /**
     * Is this the leftmost sibling node?
     */
    def isLeftmost: Boolean = lefts.isEmpty

    /**
     * Is this the rightmost sibling node?
     */
    def isRightmost: Boolean = rights.isEmpty

    /**
     * Replace the focused node.
     */
    def set(tree: Tree[A]): Location[A] = {
      Location(tree, lefts, rights, parents)
    }

    /**
     * Modify the focused node.
     */
    def modify(f: Tree[A] => Tree[A]): Location[A] = {
      set(f(tree))
    }

    /**
     * Insert to the left and focus on the new node.
     */
    def insertLeft(newTree: Tree[A]): Location[A] = {
      Location(newTree, lefts, tree :: rights, parents)
    }

    /**
     * Insert to the right and focus on the new node.
     */
    def insertRight(newTree: Tree[A]): Location[A] = {
      Location(newTree, tree :: lefts, rights, parents)
    }

    /**
     * Insert as the leftmost child and focus on the new node.
     */
    def insertLeftmostChild(newTree: Tree[A]): Location[A] = {
      Location(newTree, Nil, tree.children, descend)
    }

    /**
     * Insert as the rightmost child and focus on the new node.
     */
    def insertRightmostChild(newTree: Tree[A]): Location[A] = {
      Location(newTree, tree.children.reverse, Nil, descend)
    }

    /**
     * Insert as the nth child and focus on the new node.
     */
    def insertChild(n: Int, newTree: Tree[A]): Location[A] = {
      val (ls, rs) = tree.children.splitAt(n)
      Location(newTree, ls.reverse, rs, descend)
    }

    /**
     * Delete the focused node and then move right, otherwise left, otherwise up.
     */
    def delete: Option[Location[A]] = rights match {
      case r :: rs => Some(Location(r, lefts, rs, parents))
      case _ => lefts match {
        case l :: ls => Some(Location(l, ls, rights, parents))
        case _ => parents match {
          case p :: ps => Some(Location(Tree(p.label, Nil), p.lefts, p.rights, ps))
          case Nil     => None
        }
      }
    }

    // simplified toString
    override def toString: String = s"Location(${tree.label.toString})"
  }

  object Location {
    def forest[A](ts: Forest[A]): Option[Location[A]] = ts match {
      case t :: ts => Some(Location(t, Nil, ts, Nil))
      case Nil     => None
    }
  }

  /**
   * Form a linked forest, given a function for determining child links.
   */
  def link[A](nodes: List[A], links: A => List[A]): Forest[A] = {
    import scala.collection.mutable
    val seen = mutable.HashSet.empty[A]
    val completed = mutable.HashSet.empty[A]
    val roots = mutable.Map.empty[A, Tree[A]]
    def visit(node: A): Unit = {
      if (!seen(node)) {
        seen(node) = true;
        val linked = links(node)
        linked foreach visit
        val children = linked flatMap roots.remove
        roots += node -> Tree(node, children)
        completed(node) = true
      } else if (!completed(node)) {
        throw new RuntimeException("Cycle found at: " + node)
      }
    }
    nodes foreach visit
    roots.values.toList
  }

  /**
   * Form a linked forest from a listed hierarchy, given an ordering for levels.
   */
  @tailrec
  def hierarchy[A](nodes: List[A], stack: Forest[A] = Nil)(implicit ord: Ordering[A]): Forest[A] = {
    stack match {
      case first :: second :: rest if ord.gt(first.label, second.label) &&
        (nodes.isEmpty || ord.lteq(nodes.head, first.label)) => // squash top of stack
        hierarchy(nodes, Tree(second.label, second.children ::: List(first)) :: rest)
      case result if nodes.isEmpty => // finished, return result
        result.reverse
      case deeper => // push node on to stack
        hierarchy(nodes.tail, Tree.leaf(nodes.head) :: deeper)
    }
  }

}

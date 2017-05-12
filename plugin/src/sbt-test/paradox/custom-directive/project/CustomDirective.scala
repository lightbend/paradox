import com.lightbend.paradox.markdown.{ Directive, LeafBlockDirective, Writer }
import org.pegdown.Printer
import org.pegdown.ast.{ DirectiveNode, Visitor }

object CustomDirective extends (Writer.Context => CustomDirective) {
  def apply(context: Writer.Context): CustomDirective = CustomDirective(context.properties)
}

case class CustomDirective(properties: Map[String, String]) extends LeafBlockDirective("custom") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    printer.println.print(properties.getOrElse("custom.content", ""))
  }
}

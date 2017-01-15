object Snippets {
  // #indented
  def indented = {
    1 + 2
  }
  // #indented

  //#symbols-at-eol        ¯\_(ツ)_/¯
  val symbols = Seq('symbols, Symbol("@"), 'EOL)
  //#symbols-at-eol        ¯\_(ツ)_/¯

  //#space-after-marker
  val spacy = "Please do not remove ending spaces after these markers"
  //#space-after-marker

  val config = """
    #config
    snippets {
      test = 1
    }
    #config
    """

  //#foo
  val foo = 42
  //#foo
}

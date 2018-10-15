package ohnosequences.db.ncbitaxonomy.test

class TreeDump extends NCBITaxonomyTest("TreeDump") {

  test("readTreeFrom ∘ dumpTreeTo == id") {

    data.taxTrees.zipWithIndex foreach {
      case (tree, i) =>
        val (dataFile, shapeFile) = dumpTreeTo(
          tree,
          data.dataTempFile(i),
          data.shapeTempFile(i)
        )
        val result = readTreeFrom(dataFile, shapeFile)

        assert { tree == result }
    }
  }

}

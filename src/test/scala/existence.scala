package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy
import ncbitaxonomy.{Version, helpers}

class Existence extends NCBITaxonomyTest("Existence") {

  test("""nodes.dmp, names.dmp, data.tree and shape.tree 
       |  exist in S3 for all versions""") {

    Version.all map { version =>
      ncbitaxonomy.data.everything(version).foreach { obj =>
        assert(
          helpers.objectExists(obj),
          s"S3 object ${obj} does not exist for version ${version}"
        )
      }
    }
  }
}

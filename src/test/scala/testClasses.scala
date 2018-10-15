package ohnosequences.db.ncbitaxonomy.test
import org.scalatest.{CancelAfterFailure, FunSuite}

class NCBITaxonomyTest(val name: String)
    extends FunSuite
    with CancelAfterFailure {

  override final val suiteName: String =
    s"NCBI Taxonomy - ${name}"
}

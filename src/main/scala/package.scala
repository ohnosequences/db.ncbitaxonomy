package ohnosequences.api

import ohnosequences.db.ncbitaxonomy.Version

package object ncbitaxonomy {

  type DBVersion = Version

  val dbVersions: Set[DBVersion] = {

    import Version._
    Set(_0_0_1, _0_1_0)
  }

  private[ncbitaxonomy] type +[A, B] =
    Either[A, B]
}

package ohnosequences.db.ncbitaxonomy.test

import org.scalatest.{CancelAfterFailure, Suites}

class MirrorAndCheck
    extends Suites(
      // Ensure mirroring before checking files
      new Mirror,
      new CheckOldVersions,
      new CheckFullTaxonomy
    )

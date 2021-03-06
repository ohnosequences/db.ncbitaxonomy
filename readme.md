# NCBI Taxonomy

Mirror of NCBI Taxonomy data. The mirrored files are at

```
s3://resources.ohnosequences.com/db/ncbitaxonomy/unstable/<version>/names.dmp
s3://resources.ohnosequences.com/db/ncbitaxonomy/unstable/<version>/nodes.dmp
```

The taxonomic tree from those files is available in two separate files (data and shape are splited) at:
```
s3://resources.ohnosequences.com/db/ncbitaxonomy/unstable/<version>/data.tree
s3://resources.ohnosequences.com/db/ncbitaxonomy/unstable/<version>/shape.tree
```

where `<version>` matches one of the [releases of this repository][db.ncbitaxonomy-releases]; these are easily accessible as instances of the `Version` type.

## Installation

Just add

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"
libraryDependencies += "ohnosequences" %% "db.ncbitaxonomy" % "x.y.z"
```

to your `sbt` dependencies, where `x.y.z` is the version of the [latest release][latest-release].

## Data Source

All input data is under `ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/`. We are mostly interested in `taxdump*` files, for which there's a [readme][taxdump-readme]. We need to get the contents of [taxdump.tar.gz][taxdump-archive]; after extracing we should see

```
citations.dmp
delnodes.dmp
division.dmp
gencode.dmp
merged.dmp
names.dmp
nodes.dmp
readme.txt
```

## Data Structure

All `*.dmp` files are csv-like with

- no headers
- **row separator** `\t|\n`
- **field separator** `\t|\t`

Sample rows from `names.dmp`:

```
1	|	all	|		|	synonym	|
1	|	root	|		|	scientific name	|
2	|	Bacteria	|	Bacteria <prokaryotes>	|	scientific name	|
```

We only care about `nodes.dmp` and `names.dmp`.

### `nodes.dmp`

This file contains the tree structure (through parent ID at each row) and values linked with each node.

The fields (in the order found on the file):

- **ID** *node id in GenBank taxonomy database*
- **parentID** *parent node id in GenBank taxonomy database*
- **rank** *rank of this node (superkingdom, kingdom, ...)*
- **emblCode** *locus-name prefix; not unique*
- **divisionID** *see `division.dmp` file*
- **inheritedDiv**  *(1 or 0) 1 if node inherits division from parent*
- **geneticCodeID** *see `gencode.dmp` file*
- **inheritedGeneticCode** *(1 or 0) 1 if node inherits genetic code from parent*
- **mitochondrialGeneticCodeID** *see `gencode.dmp` file*
- **inheritedMitochondrialGeneticCode** *(1 or 0) 1 if node inherits mitochondrial gencode from parent*
- **GenBankHidden** *(1 or 0) 1 if name is suppressed in GenBank entry lineage*
- **hiddenSubtreeRoot** *(1 or 0) 1 if this subtree has no sequence data yet*
- **comments** *free-text comments and citations*

Sample rows:

```
283     |       80864   |       genus   |               |       0       |       1       |       11      |       1       |       0       |       1       |       0       |       0       |               |
285     |       283     |       species |       CT      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |
286     |       135621  |       genus   |               |       0       |       1       |       11      |       1       |       0       |       1       |       0       |       0       |               |
287     |       136841  |       species |       PA      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |
```

### `names.dmp`

This file contains the names linked with a node via taxonomy ID. The file looks to be sorted by ID, and thus all names of a given node make a contiguous block.

The fields (in the order found on the file):

- **ID** *the id of node associated with this name*
- **name** *name itself*
- **uniqueName** *the unique variant of this name if name not unique*
- **nameType** *synonym, common name, ...*

Sample rows:

```
24      |       ATCC 8071       |               |       type material   |
24      |       Alteromonas putrefaciens        |               |       synonym |
24      |       Alteromonas putrefaciens (ex Derby and Hammer) Lee et al. 1981  |               |       authority       |
24      |       Alteromonas putrifaciens        |               |       misspelling     |
24      |       CCUG 13452 D    |               |       type material   |
24      |       CFBP 3033       |               |       type material   |
24      |       CFBP 3034       |               |       type material   |
24      |       CIP 80.40       |               |       type material   |
24      |       DSM 6067        |               |       type material   |
24      |       IFO 3908        |               |       type material   |
24      |       JCM 20190       |               |       type material   |
24      |       JCM 9294        |               |       type material   |
24      |       LMG 2268        |               |       type material   |
24      |       NBRC 3908       |               |       type material   |
24      |       NCIB 10471      |               |       type material   |
24      |       NCIMB 10471     |               |       type material   |
24      |       NCTC 12960      |               |       type material   |
24      |       Pseudomonas putrefaciens        |               |       synonym |
24      |       Shewanella putrefaciens |               |       scientific name |
24      |       Shewanella putrefaciens (Lee et al. 1981) MacDonell and Colwell 1986    |               |       authority       |
24      |       Shewanella putrifaciens |               |       misspelling     |
24      |       strain Hammer 95        |               |       type material   |
```

## Data Versioning

The data source from NCBI FTP (see [Data Source](#data-source)) has a monthly release (with few exceptions). Monthly releases can be checked in their [archive][ncbi-releases]. However, we would like to
have a versioning system (firstly, to gather knowledge and be able to compare versions, and secondly to not depend on their archive, which could be erased at any moment). In their [website][website-guide], they affirm that "New taxa are added to the Taxonomy database as data are deposited for them" every time they update the taxonomy.

## NCBI Tree

We expose methods to dump and read the two serialized files: `data.tree` and `shape.tree`:

### Write

```scala
import ohnosequences.db.ncbitaxonomy._
import java.io.File

// These are existing files
val dataFile  = new File("./data/0.1.0/data.tree")
val shapeFile = new File("./data/0.1.0/shape.tree")

// Tree must be a TaxTree
io.dumpTaxTreeToFiles(tree, dataFile, shapeFile)
```

### Read

```scala
import ohnosequences.db.ncbitaxonomy._
import java.io.File

// These are existing files
val dataFile  = new File("./data/0.1.0/data.tree")
val shapeFile = new File("./data/0.1.0/shape.tree")

io.readTaxTreeFromFiles(dataFile, shapeFile).map { tree =>
  // do something with the tree
}
```



[ncbi-releases]: https://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump_archive/
[taxdump-readme]: ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump_readme.txt
[taxdump-archive]: ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz
[website-guide]: https://www.ncbi.nlm.nih.gov/guide/taxonomy/
[db.ncbitaxonomy-releases]: https://github.com/ohnosequences/db.ncbitaxonomy/releases
[sbt-buildinfo]: https://github.com/sbt/sbt-buildinfo
[latest-release]: https://github.com/ohnosequences/db.ncbitaxonomy/releases/latest

# NCBI Taxonomy

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

The fields (in the order found on the file):

- **ID** *node id in GenBank taxonomy database*
- **parentID** *parent node id in GenBank taxonomy database*
- **rank** *rank of this node (superkingdom, kingdom, ...)*
- **emblCode** *locus-name prefix; not unique*
- **divisionID** *see `division.dmp` file*
- **inheritedDiv**  *(1 or 0) 1 if node inherits division from parent*
- **geneticCodeID** *see `gencode.dmp` file*
- **inheritedGeneticCode *(1 or 0) *1 if node inherits genetic code from parent*
- **mitochondrialGeneticCodeID** *see `gencode.dmp` file*
- **inheritedMitochondrialGeneticCode** *(1 or 0) 1 if node inherits mitochondrial gencode from parent*
- **GenBankHidden** *(1 or 0) 1 if name is suppressed in GenBank entry lineage*
- **hiddenSubtreeRoot** *(1 or 0) *1 if this subtree has no sequence data yet*
- **comments** *free-text comments and citations*

Sample rows:

```
283     |       80864   |       genus   |               |       0       |       1       |       11      |       1       |       0       |       1       |       0       |       0       |               |
285     |       283     |       species |       CT      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |
286     |       135621  |       genus   |               |       0       |       1       |       11      |       1       |       0       |       1       |       0       |       0       |               |
287     |       136841  |       species |       PA      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |
```

### `names.dmp`

...

[taxdump-readme]: ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump_readme.txt
[taxdump-archive]: ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz
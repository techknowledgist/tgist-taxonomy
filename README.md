# TGist Taxonomy Creator

Simplistic and unfinished code that creates a taxonomy from a list of terms, their features and relations between those terms.

Dependencies:

- The object exploration code by Dimitris Andreou (https://github.com/DimitrisAndreou/memory-measurer). Make sure that the following jars are in your classpath: `dist/objectexplorer.jar`, `lib/guava-r09.jar` and `lib/jsr305.jar`. This code is used for debugging purposes only and technically you will not need this code when you check out the master branch of this repository.

In a bash shell:

```bash
$ corpus=/DATA/techwatch/SignalProcessing
$ java -jar dist/TGistTaxonomy.jar --create SignalProcessing taxonomies/SignalProcessing $corpus/classify.MaxEnt.out.s4.scores.sum.az $corpus/NB.IG50.test1.woc.9999.results.classes /DATA/techwatch/SignalProcessing.txt.gz
```

```bash
$ java -jar dist/TGistTaxonomy.jar --create <TaxonomyName> <DATA_DIR>
```

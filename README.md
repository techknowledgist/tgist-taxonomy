# TGist Taxonomy Creator

Simplistic and unfinished code that creates a taxonomy from a small corpus, where the corpus is represented by a list of terms and feature vectors and roles associated with those terms.

Dependencies:

- The object exploration code by Dimitris Andreou (https://github.com/DimitrisAndreou/memory-measurer). Make sure that the following jars are in your classpath: `dist/objectexplorer.jar`, `lib/guava-r09.jar` and `lib/jsr305.jar`. This code is used for debugging purposes only and technically you will not need this code when you check out the master branch of this repository.


### Data required

Creating a taxonomy requires a list of terms with technology scores, feature vectors for those terms and a list of terms with their roles. These three should be together in one directory and are expected to have the following names:

| filename | description |
| --- | --- |
| classify.MaxEnt.out.s4.scores.sum.az | list of terms |
| feastures.txt.gz | feature vectors for all terms |
| NB.IG50.test1.woc.9999.results.classes | list of terms with their roles |

*TODO: add an explanation on how to generate these files.*


### Creating a taxonomy

The one-command way to create a new taxonomy from a directory with the needed input data is:

```sh
> java -jar dist/TGistTaxonomy.jar --create <TaxonomyLocation> <DataLocation>
```

Here, `<DataLocation>` is the directory with the three required files mentioned above and `<TaxonomyLocation>` is the location of the new taxonomy, if the path already exists the program exits with a warning.

Using the `--create` option is a shorthand for four separate commands:

```sh
> java -jar dist/TGistTaxonomy.jar --init <TaxonomyLocation> <DataLocation>
> java -jar dist/TGistTaxonomy.jar --import <TaxonomyLocation>
> java -jar dist/TGistTaxonomy.jar --build-hierarchy <TaxonomyLocation>
> java -jar dist/TGistTaxonomy.jar --add-relations <TaxonomyLocation>
```

With `--init` the taxonomy is initialized, which boils down to creating a directory with in it one file named `properties.txt` which stores a short name for the taxonomy (the base name of the path where the taxonomy is created) and the location of the input data directory. With `--import` the data in the input directory are imported into the taxonomy directory. Only terms with a minimal technology score and minimum frequency are added and only the feature vectors and roles for those terms are added (which reduces the size of the data significantly).  Finally, with `--build-hierarchy` and `--add-relations` the taxonomy's hierarchy is built and relations between terms are added.

During the above processing the following files are created inside the taxonomy:

| option | files created |
| --- | --- |
| --init | properties.txt |
| --import | technologies.txt, features.txt, act.txt |
| --build-hierarchy | hierarchy.txt |
| --add-relations | relations-cooc.txt, relations-term.txt |


### Browsing a taxonomy

To browse a taxonomy do the following:

```sh
> java -jar dist/TGistTaxonomy.jar --browse <TaxonomyLocation>
```

You will get a splash screen and some limited functionality for navigating the taxonomy. Enter `q` followed by a return to exit the browser.

*TODO: document this better once some minimal improvements are made.*

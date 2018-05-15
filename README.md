## How to get started
Fork this and do a `cd src`. You can run the following commands:

1. `MyClassifier training.csv testing.csv C` where `C` is in `{NB, DT}` for a Naive Bayes Classifier and Decision Tree classifier respectively. If you are using the classifier with `DT` (a decision tree), and you would like to view a textual representation of the tree, you are free to append the flag `--printTree` on the end of this command. The tree will print before the testing data is classified as yes or no.
2. `MyClassifier data.txt --stratify`. This breaks up the rows of `data.txt` into 10 folds with "yes" and "no" rows evenly distributed among the folds.
3. `MyClassifier data.txt C --accuracy`. Given that `data.txt` is broken up into 10 folds with the heading

```c++
fold 1
...
fold 10
```


for each, running this command will output the accuracy of the given Classifier `C` on each fold, and then finally give an average overall accuracy as the last line of output. 


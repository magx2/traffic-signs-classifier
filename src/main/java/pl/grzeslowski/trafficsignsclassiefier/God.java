package pl.grzeslowski.trafficsignsclassiefier;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.MultiImageTransform;
import org.datavec.image.transform.ShowImageTransform;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class God {
    private String pathToData = "D:\\Programowanie\\deep_learning\\znaki_drogowe";
    private String metaDataFileName = "traffic-signs.csv";
    private String separator = ",";
    private String[] allowedExtensions = NativeImageLoader.ALLOWED_FORMATS;
    private int height = 170;
    private int width = 300;
    private int channels = 3;
    private int outputNum = 2;
    private Sign signToFind = new Sign("B", "36");
    private double learningRate = 0.1;
    private int numHiddenNodes = 100;
    private int epochs = 10;

    public static void main(String[] args) throws Exception {
        final God god = new God();
        god.run();
    }

    private void run() throws Exception {
        final File metaData = new File(pathToData + File.separator + metaDataFileName);
        checkArgument(metaData.exists());

        Set<Example> examples = loadExamples(metaData);

        System.out.printf("# examples: %s%n", examples.size());
        System.out.printf("Signs types: %s%n",
                examples.stream()
                        .flatMap(e -> e.getSigns().stream())
                        .map(Sign::getType)
                        .distinct()
                        .collect(Collectors.joining(", ")));
        System.out.printf("Occurrences of each sign type %s%n",
                examples.stream()
                        .flatMap(e -> e.getSigns().stream())
                        .collect(
                                Collectors.groupingBy(
                                        Function.identity(), Collectors.counting()
                                )
                        ));

        // DL4J - image pipeline
        // see: https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/dataExamples/ImagePipelineExample.java
        final File parentDir = new File(pathToData);
        checkArgument(parentDir.exists());
        checkArgument(parentDir.isDirectory());

        //Files in directories under the parent dir that have "allowed extensions" split needs a random number generator for reproducibility when splitting the files into train and test
        FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions, new Random(1337));

        //You do not have to manually specify labels. This class (instantiated as below) will
        //parse the parent dir and use the name of the subdirectories as label/class names
        ParentPathLabelGenerator labelMakerOld = new ParentPathLabelGenerator();
        PathLabelGenerator labelMaker = new LabelGenerator(signToFind, examples);
        //The balanced path filter gives you fine tune control of the min/max cases to load for each class
        //Below is a bare bones version. Refer to javadocs for details
        BalancedPathFilter pathFilter = new BalancedPathFilter(new Random(1337), allowedExtensions, labelMaker);

        //Split the image files into train and test. Specify the train test split as 80%,20%
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, 80, 20);
        InputSplit trainData = filesInDirSplit[0];
        InputSplit testData = filesInDirSplit[1];

        //Specifying a new record reader with the height and width you want the images to be resized to.
        //Note that the images in this example are all of different size
        //They will all be resized to the height and width specified below
        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

        //Often there is a need to transforming images to artificially increase the size of the dataset
        //DataVec has built in powerful features from OpenCV
        //You can chain transformations as shown below, write your own classes that will say detect a face and crop to size
        /*ImageTransform transform = new MultiImageTransform(randNumGen,
            new CropImageTransform(10), new FlipImageTransform(),
            new ScaleImageTransform(10), new WarpImageTransform(10));
            */


        // Neural net
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(1337)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(height * width).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax").weightInit(WeightInit.XAVIER)
                        .nIn(numHiddenNodes).nOut(outputNum).build())
                .pretrain(false).backprop(true)
                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates

        //You can use the ShowImageTransform to view your images
        //Code below gives you a look before and after, for a side by side comparison
        ImageTransform transform = new MultiImageTransform(new Random(1337), new ShowImageTransform("Display - before "));

        //Initialize the record reader with the train data and the transform chain
        recordReader.initialize(trainData, transform);
        //convert the record reader to an iterator for training - Refer to other examples for how to use an iterator
        DataSetIterator trainIter = new RecordReaderDataSetIterator(recordReader, 10, 1, outputNum);
//        trainIter.reset();


        while (trainIter.hasNext()) {
            DataSet ds = trainIter.next();

            for (int n = 0; n < epochs; n++) {
                model.fit(ds);
            }
        }


//        for (int n = 0; n < epochs; n++) {
//            model.fit(trainIter);
//        }


        System.out.println("Evaluate model....");

        //transform = new MultiImageTransform(randNumGen,new CropImageTransform(50), new ShowImageTransform("Display - after"));
        //recordReader.initialize(trainData,transform);
        recordReader.initialize(testData);
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, 10, 1, outputNum);
        testIter.reset();

        Evaluation eval = new Evaluation(outputNum);
        while (testIter.hasNext()) {
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            INDArray predicted = model.output(features, false);

            eval.eval(lables, predicted);
        }

    }

    private Set<Example> loadExamples(File metaData) throws IOException {
        try (Stream<String> stream = Files.lines(metaData.toPath())) {
            return stream.map(line -> line.split(separator))
                    .map(this::parseExample)
                    .collect(Collectors.toSet());
        }
    }

    private Example parseExample(String[] line) {
        checkArgument(line.length >= 2, "Line length = " + line.length);
        final File photoFile = new File(pathToData + File.separator + line[0]);

        final Set<Sign> signs = new HashSet<>();
        for (int i = 1; i < line.length; i++) {
            String sign = line[i];
            if (sign.isEmpty()) {
                break;
            }
            checkArgument(sign.length() >= 2, "Sign = " + sign);
            final String type = sign.charAt(0) + "";
            final String subType = sign.substring(1);
            signs.add(new Sign(type, subType));
        }

        return new Example(photoFile, signs);
    }

}

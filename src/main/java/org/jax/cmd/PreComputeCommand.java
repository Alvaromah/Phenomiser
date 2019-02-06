package org.jax.cmd;

import com.beust.jcommander.Parameter;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.AbstractResources;
import org.jax.services.ComputedResources;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PreComputeCommand extends PhenomiserCommand {

    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file")
    private String diseasePath;
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath;
    @Parameter(names = {"-numThreads"}, description = "specify the number of threads")
    private Integer numThreads = 4;
    @Parameter(names = {"-sampling", "--sampling-range"},
            description = "range of HPO terms to create similarity distributions for",
            arity = 2)
    private List<Integer> sampling = Arrays.asList(1, 15);
    @Parameter(names = {"-debug"}, description = "use debug mode")
    private boolean debug = false;

    @Override
    public void run() {

        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, hpoParser.getHpo());
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, hpoParser.getHpo());

        Properties properties = new Properties();
        properties.setProperty("numThreads", Integer.toString(numThreads));
        if (cachePath != null) {
            properties.setProperty("cachingPath", cachePath);
        }

        if (sampling.get(0) > sampling.get(1)) {
            System.exit(1);
        }
        properties.setProperty("sampleMin", Integer.toString(sampling.get(0)));
        properties.setProperty("sampleMax", Integer.toString(sampling.get(1)));

        AbstractResources resources = new ComputedResources(hpoParser, diseaseParser, properties, debug);
        resources.init();
    }
}

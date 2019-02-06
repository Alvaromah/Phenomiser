package org.jax.cmd;

import com.beust.jcommander.Parameter;
import org.jax.Phenomiser;
import org.jax.PhenomiserApp;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class QueryCommand extends PhenomiserCommand {
    private static Logger logger = LoggerFactory.getLogger(QueryCommand.class);
    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file")
    private String diseasePath;
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath;
    @Parameter(names = {"-db", "--diseaseDB"},
            description = "choose disease database [OMIM,ORPHA]")
    private String diseaseDB;
    @Parameter(names = {"-q", "--query-terms"}, description = "specify HPO terms to query")
    private String query;

    @Parameter(names = {"-o", "--output"}, description = "specify output path")
    private String outPath;

    private AbstractResources resources;


    @Override
    public void run() {
        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, hpoParser.getHpo());
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, hpoParser.getHpo());
        resources = new CachedResources(hpoParser, diseaseParser, cachePath);
        resources.init();
        Phenomiser.setResources(resources);

        List<TermId> queryList = Arrays.stream(query.split(",")).map(TermId::of).collect(Collectors.toList());
        List<DiseaseDB> db = Arrays.stream(diseaseDB.split(",")).map(DiseaseDB::valueOf).collect(Collectors.toList());
        List<Item2PValue<TermId>> result = Phenomiser.query(queryList, db);

        //output query result
        if (!result.isEmpty()) {
            write_query_result(result, outPath);
        }
    }

    public static Writer getWriter(String path) {
        Writer writer;
        try {
            writer = new FileWriter(new File(path));
        } catch (Exception e) {
            logger.info("out path not found. writing to console: ");
            writer = new OutputStreamWriter(System.out);
        }
        return writer;
    }

    public void write_query_result(List<Item2PValue<TermId>> result, @Nullable String outPath) {

//        if (adjusted_p_value == null) {
//            return;
//        }

        Writer writer = getWriter(outPath);

        try {
            writer.write("diseaseId\tdiseaseName\tp\tadjust_p\n");
        } catch (IOException e) {
            logger.error("io exception during writing header. writing output aborted.");
            return;
        }
        List<Item2PValue<TermId>> newList = new ArrayList<>(result);
        Collections.sort(newList);

        newList.stream().forEach(e -> {
            try {
                writer.write(e.getItem().getValue());
                writer.write("\t");
                writer.write(resources.getDiseaseMap().get(e.getItem()).getName());
                writer.write("\t");
                writer.write(Double.toString(e.getRawPValue()));
                writer.write("\t");
                writer.write(Double.toString(e.getAdjustedPValue()));
                writer.write("\n");
            } catch (IOException exception) {
                logger.error("IO exception during writing out adjusted p values");
            }

        });

        try {
            writer.close();
        } catch (IOException e) {
            logger.error("IO exception during closing writer");
        }
    }
}

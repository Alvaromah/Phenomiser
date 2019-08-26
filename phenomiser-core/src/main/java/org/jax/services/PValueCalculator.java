package org.jax.services;

import org.jax.model.Item2PValueAndSimilarity;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.monarchinitiative.phenol.stats.MultipleTestingCorrection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class PValueCalculator  {

    private Map<Integer, ScoreDistribution> scoreDistributions;

    private Map<TermId, Double> similarityScores;

    private Map<Integer, TermId> diseaseIndexToDisease;

    private int queryTermCount;

    public PValueCalculator(int queryTermCount, Map<TermId, Double> similarityScores, AbstractResources resources) {
        //Above 10, score distributions are identical to 10
        this.queryTermCount = Math.min(queryTermCount, 10);
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        this.diseaseIndexToDisease = resources.getDiseaseIndexToDisease();
    }

    public Map<TermId, Item2PValueAndSimilarity<TermId>> calculatePValues() {

        Map<TermId, Item2PValueAndSimilarity<TermId>> p_values = new
                HashMap<>();
        similarityScores.forEach((diseaseId, similarityScore) -> {
            if (scoreDistributions.containsKey(queryTermCount) &&
                    scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(diseaseId.hashCode()) != null) {
                double p = scoreDistributions.get(queryTermCount)
                        .getObjectScoreDistribution(diseaseId.hashCode())
                        .estimatePValue(similarityScore);
                if(diseaseIndexToDisease.get(diseaseId.hashCode()).getValue().equals("OMIM:612642")) {
                    System.err.println("SCORE=" + scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(diseaseId.hashCode()));
                }

                p_values.put(diseaseId, new Item2PValueAndSimilarity<>
                        (diseaseId, p, similarityScore));
            }
        });

        return p_values;
    }

}

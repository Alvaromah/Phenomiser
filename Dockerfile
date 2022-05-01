FROM maven:3.3-jdk-8

COPY . Phenomiser
RUN cd /Phenomiser && mvn package

RUN cd /Phenomiser/phenomiser-cli/target && \
    wget -O hp.obo http://purl.obolibrary.org/obo/hp.obo && \
    wget -O phenotype.hpoa http://purl.obolibrary.org/obo/hp/hpoa/phenotype.hpoa

WORKDIR /Phenomiser/phenomiser-cli/target

ENTRYPOINT ["/bin/bash"]

# Samples
#ENTRYPOINT ["java", "-jar", "-Xmx12g", "phenomiser-cli-0.1.1.jar", "precompute", "-hpo", "hp.obo", "-da", "phenotype.hpoa", "-db", "OMIM", "-cachePath", "/cache-data", "-sampling", "2", "2"]
#ENTRYPOINT ["java", "-jar", "-Xmx12g", "phenomiser-cli-0.1.1.jar", "precompute", "-hpo", "hp.obo", "-da", "phenotype.hpoa", "-db", "OMIM", "-cachePath", "/cache-data", "-sampling", "1", "10"]
#ENTRYPOINT ["java", "-jar", "-Xmx12g", "phenomiser-cli-0.1.1.jar", "precompute", "-hpo", "hp.obo", "-da", "phenotype.hpoa", "-db", "ORPHA", "-cachePath", "/cache-data", "-sampling", "2", "2"]
#ENTRYPOINT ["java", "-jar", "-Xmx12g", "phenomiser-cli-0.1.1.jar", "precompute", "-hpo", "hp.obo", "-da", "phenotype.hpoa", "-db", "ORPHA", "-cachePath", "/cache-data", "-sampling", "1", "10"]
#ENTRYPOINT ["java", "-jar", "-Xmx12g", "phenomiser-cli-0.1.1.jar", "query", "-hpo", "hp.obo", "-da", "phenotype.hpoa", "-cachePath", "/cache-data", "-query", "HP:0002133,HP:0011172"]

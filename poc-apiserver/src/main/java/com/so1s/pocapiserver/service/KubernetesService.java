package com.so1s.pocapiserver.service;

import com.so1s.pocapiserver.fabric8o.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    private KubernetesClient client;
    private final String namespace = "poc";
    private final String masterURL = "http://localhost:8080/";
    private final String oauthToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InZRSjhvci1GUW14OElCVnJZb3ZlMG51Q1RWT2tlbFJlOEpSWjRFU1pvM2sifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJwb2MiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoicG9jLXRva2VuIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6InBvYy1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNDgzMzBlZTgtZWY4OS00MjJlLTkwYTItNThhMzlhY2NmYzAyIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OnBvYzpwb2MtYWNjb3VudCJ9.PyD05lJvJiVs4OKXWSk5yX7uOGcehO-MjGDbq3f0LBs6ym4cpswds9biwmolwocU4c_507GunaLCd82y64EaZelsH6CiLSbQP1RAHaCxBC4XYQ5Zz1Q7kP9atJLGCA-Oa6Woje5oc0Z8mwDa6JLM3bj-be9mc6EuGjqvnssP3zeO6DFTWjFOonIvpDYb4GVXO6ATRZ38KpE2WbbtJEXVLFR9h8QhiKKlp04GmvhXKMVQj_UK8555_DVYsJYUI1Lo1sBtnjdf604-xF8WQQVooWe430e4HPa2kaO59CIx2-X51hKa_kIMBjaqlw768oT9e1pr4hb48qBKab79STem_g";

    public KubernetesService() {
//        Config config = new ConfigBuilder().withOauthToken(oauthToken).build();
//        this.client = new KubernetesClientBuilder().withConfig(config).build();
        this.client = new DefaultKubernetesClient();
    }

    public boolean buildImage() {
        final String jobName="poc-build-job";

        final Job job = new JobBuilder()
                .withApiVersion("batch/v1")
                .withNewMetadata()
                .withName(jobName)
                .withNamespace(namespace)
                .addToLabels("job-name", jobName)
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .addNewContainer()
                .withName(jobName)
                .withImage("poc-build:0.1")
                .withCommand("/bin/sh", "/apps/build.sh")
                .withVolumeMounts(new VolumeMountBuilder().withMountPath("/var/run/docker.sock").withName("docker-sock").build())
                .endContainer()
                .withVolumes(new VolumeBuilder().withName("docker-sock").withHostPath(new HostPathVolumeSourceBuilder().withPath("/var/run/docker.sock").build()).build())
                .withRestartPolicy("Never")
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.batch().v1().jobs().inNamespace(namespace).createOrReplace(job);

        // Get All pods created by the job
        PodList podList = client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).list();
        // Wait for pod to complete
        client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName())
                .waitUntilCondition(pod -> pod.getStatus().getPhase().equals("Succeeded"), 20, TimeUnit.MINUTES);

        return true;
    }

    public boolean updateInferenceServer() {
        try {
            client.apps().deployments().inNamespace(namespace).withName("deployment-inference-server").delete();
            System.out.print("Delete Old Version Inference Server.");
            Thread.sleep(1000 * 10);
        } catch (Exception e) {
            System.out.println("Not Delete.");
        }

        Deployment deployment = new DeploymentBuilder()
                .withApiVersion("apps/v1")
                .withNewMetadata()
                .withName("deployment-inference-server")
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withReplicas(3)
                .withNewSelector()
                .addToMatchLabels("app", "inference-server")
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withName("poc-api")
                .addToLabels("app", "inference-server")
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("my-poc-inf")
                .withImage("poc-infer:0.1")
                .addNewPort()
                .withContainerPort(5000)
                .withName("flask-port")
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.apps().deployments().inNamespace(namespace).create(deployment);

        return true;
    }
}

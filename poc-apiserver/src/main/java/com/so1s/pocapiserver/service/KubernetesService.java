package com.so1s.pocapiserver.service;

import com.so1s.pocapiserver.fabric8o.KubernetesClientBuilder;
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
    private final String namespace = "default";

    public KubernetesService() {
        this.client = new DefaultKubernetesClient();
    }

    public boolean buildImage() {
        final Job job = new JobBuilder()
                .withApiVersion("batch/v1")
                .withNewMetadata()
                    .withName("poc-build-job")
                .endMetadata()
                .withNewSpec()
                    .withNewTemplate()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("poc-build-job")
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
        final String namespace = "default";

        try {
            client.apps().deployments().inNamespace(namespace).withName("deployment-inference-server").delete();
            System.out.println("Delete Old Version Inference Server.");
            Thread.sleep(1000*10);
        } catch (Exception e) {
            System.out.println("Not Delete.");
        }

        Deployment deployment = new DeploymentBuilder()
                .withApiVersion("apps/v1")
                .withNewMetadata()
                    .withName("deployment-inference-server")
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

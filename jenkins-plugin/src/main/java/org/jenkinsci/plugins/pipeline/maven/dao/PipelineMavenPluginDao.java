/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.maven.dao;

import hudson.model.Item;
import hudson.model.Run;
import org.apache.maven.artifact.Artifact;
import org.jenkinsci.plugins.pipeline.maven.MavenArtifact;
import org.jenkinsci.plugins.pipeline.maven.MavenDependency;
import org.jenkinsci.plugins.pipeline.maven.publishers.PipelineGraphPublisher;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public interface PipelineMavenPluginDao extends Closeable {

    /**
     * Record a Maven dependency of a build.
     *  @param jobFullName            see {@link Item#getFullName()}
     * @param buildNumber            see {@link Run#getNumber()}
     * @param groupId                Maven dependency groupId
     * @param artifactId             Maven dependency artifactId
     * @param version                Maven dependency version
     * @param type                   Maven dependency type (e.g. "jar", "war", "pom", hpi"...)
     * @param scope                  Maven dependency scope ("compile", "test", "provided"...)
     * @param ignoreUpstreamTriggers see {@link PipelineGraphPublisher#isIgnoreUpstreamTriggers()} ()}
     * @param classifier             Maven dependency classifier
     */
    void recordDependency(@NonNull String jobFullName, int buildNumber,
                          @NonNull String groupId, @NonNull String artifactId, @NonNull String version, @NonNull String type, @NonNull String scope,
                          boolean ignoreUpstreamTriggers, String classifier);

    /**
     * Record a Maven parent project of a pom processed by this build of a build.
     *
     * @param jobFullName            see {@link Item#getFullName()}
     * @param buildNumber            see {@link Run#getNumber()}
     * @param parentGroupId                Maven dependency groupId
     * @param parentArtifactId             Maven dependency artifactId
     * @param parentVersion                Maven dependency version
     * @param ignoreUpstreamTriggers see {@link PipelineGraphPublisher#isIgnoreUpstreamTriggers()} ()}
     */
    void recordParentProject(@NonNull String jobFullName, int buildNumber,
                             @NonNull String parentGroupId, @NonNull String parentArtifactId, @NonNull String parentVersion,
                             boolean ignoreUpstreamTriggers);
    /**
     * Record a Maven artifact generated in a build.
     * @param jobFullName            see {@link Item#getFullName()}
     * @param buildNumber            see {@link Run#getNumber()}
     * @param groupId                Maven artifact groupId
     * @param artifactId             Maven artifact artifactId
     * @param version                Maven artifact version, the "expanded version" for snapshots who have been "mvn deploy" or equivalent
*                               (e.g. "1.1-20170808.155524-66" for "1.1-SNAPSHOT" deployed to a repo)
     * @param type                   Maven artifact type (e.g. "jar", "war", "pom", hpi"...)
     * @param baseVersion            Maven artifact version, the NOT "expanded version" for snapshots who have been "mvn deploy" or equivalent
*                               (e.g. baseVersion is "1.1-SNAPSHOT" for a "1.1-SNAPSHOT" artifact that has been deployed to a repo and expanded
*                               to "1.1-20170808.155524-66")
     * @param repositoryUrl          URL of the Maven repository on which the artifact is deployed ("mvn deploy"). {@code null} if the artifact was not deployed
     * @param skipDownstreamTriggers see {@link PipelineGraphPublisher#isSkipDownstreamTriggers()}
     * @param extension
     * @param classifier
     */
    void recordGeneratedArtifact(@NonNull String jobFullName, int buildNumber,
                                 @NonNull String groupId, @NonNull String artifactId, @NonNull String version, @NonNull String type, @NonNull String baseVersion,
                                 @Nullable String repositoryUrl, boolean skipDownstreamTriggers, String extension, String classifier);

    /**
     * TODO add {@link MavenArtifact} as {@link org.jenkinsci.plugins.pipeline.maven.cause.MavenDependencyUpstreamCause} gives these details
     * @param upstreamJobName Job that triggered the build. See {@link Item#getFullName()}.
     * @param upstreamBuildNumber Job that triggered the build. See {@link Run#getNumber()}.
     * @param downstreamJobName Job that is triggered. See {@link Item#getFullName()}.
     * @param downstreamBuildNumber Job that is triggered. See {@link Run#getNumber()}.
     */
    void recordBuildUpstreamCause(String upstreamJobName, int upstreamBuildNumber, String downstreamJobName, int downstreamBuildNumber);

    /**
     * Return the dependencies registered by the given build.
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return sorted list of maven artifacts.
     * @see #recordDependency(String, int, String, String, String, String, String, boolean, String)
     */
    @NonNull
    List<MavenDependency> listDependencies(@NonNull String jobFullName, int buildNumber);

    /**
     * Return the artifacts generated by the given build.
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return sorted list of generated maven artifacts.
     */
    @NonNull
    List<MavenArtifact> getGeneratedArtifacts(@NonNull String jobFullName, int buildNumber);

    /**
     * Sync database when a job is renamed (see {@link hudson.model.listeners.ItemListener#onRenamed(Item, String, String)})
     *
     * @param oldFullName see {@link Item#getFullName()}
     * @param newFullName see {@link Item#getFullName()}
     * @see hudson.model.listeners.ItemListener#onRenamed(Item, String, String)
     */
    void renameJob(@NonNull String oldFullName, @NonNull String newFullName);

    /**
     * Sync database when a job is deleted (see {@link hudson.model.listeners.ItemListener#onDeleted(Item)})
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @see hudson.model.listeners.ItemListener#onDeleted(Item)
     */
    void deleteJob(@NonNull String jobFullName);

    /**
     * Sync database when a build is deleted (see {@link hudson.model.listeners.RunListener#onDeleted(Run)})
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @see hudson.model.listeners.RunListener#onDeleted(Run)
     */
    void deleteBuild(@NonNull String jobFullName, int buildNumber);

    /**
     * List the downstream jobs who have a dependency on an artifact that has been generated by the given build
     * (build identified by the given {@code jobFullName}, {@code buildNumber}).
     *
     * Doesn't return the passed job in case where a pipeline consumes an artifact it also produces
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return list of job full names (see {@link Item#getFullName()})
     * @see Item#getFullName()
     * @deprecated use {@link #listDownstreamJobsByArtifact(String, int)}
     */
    @NonNull
    @Deprecated
    List<String> listDownstreamJobs(@NonNull String jobFullName, int buildNumber);

    /**
     * List the downstream jobs who have a dependency on an artifact that has been generated by the given build
     * (build identified by the given {@code jobFullName}, {@code buildNumber}).
     *
     * Doesn't return the passed job in case where a pipeline consumes an artifact it also produces
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return list of job full names (see {@link Item#getFullName()}) by {@link MavenArtifact}
     * @see Item#getFullName()
     */
    @NonNull
    Map<MavenArtifact, SortedSet<String>> listDownstreamJobsByArtifact(@NonNull String jobFullName, int buildNumber);

    /**
     * List the downstream jobs who have a dependency on the given artifact.
     *
     * @param groupId Maven artifact group ID  (see {@link Artifact#getArtifactId()})
     * @param artifactId Maven artifact id (see {@link Artifact#getArtifactId()})
     * @param version Maven artifact version (see {@link Artifact#getVersion()})
     * @param baseVersion Maven artifact (see {@link Artifact#getBaseVersion()})
     * @param type Maven artifact type (see {@link Artifact#getType()}})
     * @return list of job full names (see {@link Item#getFullName()}) by {@link MavenArtifact}
     * @see Item#getFullName()
     */
    @NonNull
    default SortedSet<String> listDownstreamJobs(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, @Nullable String baseVersion, @NonNull String type) {
        return listDownstreamJobs(groupId, artifactId, version, baseVersion, type, null);
    }

    /**
     * List the downstream jobs who have a dependency on the given artifact.
     *
     * @param groupId Maven artifact group ID  (see {@link Artifact#getArtifactId()})
     * @param artifactId Maven artifact id (see {@link Artifact#getArtifactId()})
     * @param version Maven artifact version (see {@link Artifact#getVersion()})
     * @param baseVersion Maven artifact (see {@link Artifact#getBaseVersion()})
     * @param type Maven artifact type (see {@link Artifact#getType()}})
     * @param classifier Maven artifact classifier (see {@link Artifact#getClassifier()}})
     * @return list of job full names (see {@link Item#getFullName()}) by {@link MavenArtifact}
     * @see Item#getFullName()
     */
    @NonNull
    SortedSet<String> listDownstreamJobs(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, @Nullable String baseVersion, @NonNull String type, @Nullable String classifier);

    /**
     * List the upstream jobs who generate an artifact that the given build depends on
     * (build identified by the given {@code jobFullName}, {@code buildNumber})
     *
     * Doesn't return the passed job in case where a pipeline consumes an artifact it also produces
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return list of builds : key {@link Item#getFullName()}, value: {@link Run#getNumber()}
     * @see Item#getFullName()
     */
    @NonNull
    Map<String, Integer> listUpstreamJobs(@NonNull String jobFullName, int buildNumber);
    
    /**
     * List the upstream jobs who generate an artifact that the given build depends
     * on, including transitive dependencies (build identified by the given
     * {@code jobFullName}, {@code buildNumber})
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @return list of job full names (see {@link Item#getFullName()})
     * @see Item#getFullName()
     */
    @NonNull
    Map<String, Integer> listTransitiveUpstreamJobs(@NonNull String jobFullName, int buildNumber);

    /**
     * List the upstream jobs who generate an artifact that the given build depends
     * on, including transitive dependencies (build identified by the given
     * {@code jobFullName}, {@code buildNumber})
     * 
     * Use a memory for already known upstreams to boost performance
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @param upstreamMemory see {@link UpstreamMemory}  - if called for several jobs in a loop we save the already known upstreams
     * @return list of job full names (see {@link Item#getFullName()})
     * @see Item#getFullName()
     */
    @NonNull
    Map<String, Integer> listTransitiveUpstreamJobs(@NonNull String jobFullName, int buildNumber, UpstreamMemory upstreamMemory);

    /**
     * Routine task to cleanup the database and reclaim disk space (if possible in the underlying database).
     */
    void cleanup();

    /**
     * Human readable toString
     */
    String toPrettyString();


    /**
     * Update the database with build result details.
     *
     * @param jobFullName see {@link Item#getFullName()}
     * @param buildNumber see {@link Run#getNumber()}
     * @param buildResultOrdinal see {@link hudson.model.Result#ordinal}
     * @param startTimeInMillis see {@link Run#getStartTimeInMillis()}
     * @param durationInMillis see {@link Run#getDuration()}
     */
    void updateBuildOnCompletion(@NonNull String jobFullName, int buildNumber, int buildResultOrdinal, long startTimeInMillis, long durationInMillis);

    /**
     * Indicates if the underlying database is production grade enough for the workload.
     *
     * H2 database is not enough for production grade workloads of the pipeline-maven-plugin.
     *
     * @return {@code false} if the underlying database is not production grade enough for the workload
     */
    boolean isEnoughProductionGradeForTheWorkload();
}

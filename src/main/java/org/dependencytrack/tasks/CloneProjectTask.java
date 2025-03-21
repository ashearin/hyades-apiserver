/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.tasks;

import alpine.common.logging.Logger;
import alpine.event.framework.Event;
import alpine.event.framework.Subscriber;
import org.dependencytrack.event.CloneProjectEvent;
import org.dependencytrack.model.Project;
import org.dependencytrack.model.WorkflowState;
import org.dependencytrack.persistence.QueryManager;
import org.dependencytrack.resources.v1.vo.CloneProjectRequest;
import org.slf4j.MDC;

import java.util.Date;
import java.util.UUID;

import static org.dependencytrack.common.MdcKeys.MDC_EVENT_TOKEN;
import static org.dependencytrack.common.MdcKeys.MDC_PROJECT_UUID;
import static org.dependencytrack.model.WorkflowStatus.PENDING;
import static org.dependencytrack.model.WorkflowStep.PROJECT_CLONE;

public class CloneProjectTask implements Subscriber {

    private static final Logger LOGGER = Logger.getLogger(CloneProjectTask.class);

    /**
     * {@inheritDoc}
     */
    public void inform(final Event e) {
        if (e instanceof final CloneProjectEvent event) {
            final CloneProjectRequest request = event.getRequest();
            final UUID chainIdentifier = event.getChainIdentifier();
            try (var ignoredMdcProjectUuid = MDC.putCloseable(MDC_PROJECT_UUID, request.getProject());
                 var ignoredMdcEventToken = MDC.putCloseable(MDC_EVENT_TOKEN, event.getChainIdentifier().toString());
                 final var qm = new QueryManager()) {
                WorkflowState workflowState = qm.updateStartTimeIfWorkflowStateExists(chainIdentifier, PROJECT_CLONE);
                if (workflowState == null) {
                    final var now = new Date();
                    workflowState = new WorkflowState();
                    workflowState.setStep(PROJECT_CLONE);
                    workflowState.setStatus(PENDING);
                    workflowState.setToken(chainIdentifier);
                    workflowState.setStartedAt(now);
                    workflowState.setUpdatedAt(now);
                    qm.persist(workflowState);
                }

                try {
                    LOGGER.info("Cloning project for version %s".formatted(request.getVersion()));
                    final Project project = qm.clone(
                            UUID.fromString(request.getProject()),
                            request.getVersion(),
                            request.includeTags(),
                            request.includeProperties(),
                            request.includeComponents(),
                            request.includeServices(),
                            request.includeAuditHistory(),
                            request.includeACL(),
                            request.includePolicyViolations(),
                            request.makeCloneLatest()
                    );

                    qm.updateWorkflowStateToComplete(workflowState);
                    LOGGER.info("Cloned project for version %s into project %s".formatted(project.getVersion(), project.getUuid()));
                } catch (Exception ex) {
                    LOGGER.error("Failed to clone project", ex);
                    qm.updateWorkflowStateToFailed(workflowState, ex.getMessage());
                }
            }
        }
    }
}

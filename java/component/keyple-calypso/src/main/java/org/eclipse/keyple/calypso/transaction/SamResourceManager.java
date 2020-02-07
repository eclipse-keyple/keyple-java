package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;

public interface SamResourceManager {

    public enum AllocationMode {
        BLOCKING, NON_BLOCKING
    }

    /**
     * Allocate a SAM resource from the specified SAM group.
     * <p>
     * In the case where the allocation mode is BLOCKING, this method will wait until a SAM resource
     * becomes free and then return the reference to the allocated resource. However, the BLOCKING
     * mode will wait a maximum time defined in tenths of a second by MAX_BLOCKING_TIME.
     * <p>
     * In the case where the allocation mode is NON_BLOCKING and no SAM resource is available, this
     * method will return null.
     * <p>
     * If the samGroup argument is null, the first available SAM resource will be selected and
     * returned regardless of its group.
     *
     * @param allocationMode the blocking/non-blocking mode
     * @param samIdentifier the targeted SAM identifier
     * @return a SAM resource
     * @throws KeypleReaderException if a reader error occurs
     */
     SamResource allocateSamResource(AllocationMode allocationMode,
                                     SamIdentifier samIdentifier) throws KeypleReaderException;

    /**
     * Free a previously allocated SAM resource.
     *
     * @param samResource the SAM resource reference to free
     */
     void freeSamResource(SamResource samResource);

}

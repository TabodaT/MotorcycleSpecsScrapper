package com.motointel.app.jobs;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Process-wide registry of in-flight job cancellation flags.
 *
 * <p>This is a singleton {@link Component} shared between two kinds of threads:
 * <ul>
 *   <li><b>HTTP request threads</b> handle {@code POST /api/jobs/{id}/cancel} and call
 *       {@link #requestCancel(Long)} to set a job's flag;</li>
 *   <li><b>worker threads</b> (e.g. the async catalog crawl executor) call
 *       {@link #register(Long)} when they begin, poll {@link #isCancelled(Long)} between
 *       work items, and call {@link #clear(Long)} in a finally block when they finish.</li>
 * </ul>
 *
 * <p>All state lives in a {@link ConcurrentHashMap} of {@link AtomicBoolean} flags, so reads and
 * writes are non-blocking and thread-safe without external synchronization.
 *
 * <p><b>Safe-before-register guarantee:</b> an HTTP thread may call {@link #requestCancel(Long)}
 * before the worker thread has had a chance to {@link #register(Long)}. Because
 * {@link #requestCancel(Long)} uses {@code computeIfAbsent} to create the flag if it is missing,
 * the request is never lost: when the worker later registers, {@code computeIfAbsent} returns the
 * already-set flag and the worker observes the cancellation on its next poll.
 */
@Component
public class JobCancellationRegistry {

    private final ConcurrentHashMap<Long, AtomicBoolean> flags = new ConcurrentHashMap<>();

    /**
     * Registers the given job, returning its cancellation flag (creating one if absent).
     * A worker thread calls this when it starts; if a cancel was already requested, the returned
     * flag will already read {@code true}.
     */
    public AtomicBoolean register(Long jobId) {
        return flags.computeIfAbsent(jobId, id -> new AtomicBoolean(false));
    }

    /**
     * Requests cancellation of the given job. Safe to call before the worker registers — the flag
     * is created on demand via {@code computeIfAbsent} so the request is never lost.
     */
    public void requestCancel(Long jobId) {
        flags.computeIfAbsent(jobId, id -> new AtomicBoolean(false)).set(true);
    }

    /** Returns {@code true} if cancellation has been requested for the given job. */
    public boolean isCancelled(Long jobId) {
        AtomicBoolean flag = flags.get(jobId);
        return flag != null && flag.get();
    }

    /** Removes the job's entry. A worker calls this in a finally block when it finishes. */
    public void clear(Long jobId) {
        flags.remove(jobId);
    }
}

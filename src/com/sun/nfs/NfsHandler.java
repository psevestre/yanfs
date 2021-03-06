/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.nfs;

import java.io.*;
import com.sun.rpc.*;

/**
 *  This handler is implemented by the NFS application
 *  if it wishes to be notifed of retransmissions.
 *  A good example is an NFS client that displays
 *  "NFS Server not responding" and "NFS server OK"
 */
public abstract class NfsHandler extends RpcHandler {

    /**
     * Called when an NFS request has timed out
     *
     * The RPC code will retransmit NFS requests
     * until the server responds.  The initial
     * retransmission timeout is set by the NFS
     * code and increases exponentially with
     * each retransmission until an upper bound
     * of 30 seconds is reached, e.g. timeouts
     * will be 1, 2, 4, 8, 16, 30, 30, ... sec.
     * <p>
     * An instance of the NfsHandler class is
     * registered with the setHandler method of
     * the NFS XFileExtensionAccessor.
     *
     * @param server The name of the server to which the
     *        request was sent.
     * @param retries The number of times the request has
     *        been retransmitted.  After the first timeout
     *        retries will be zero.
     * @param wait Total time since first call in milliseconds
     *        (cumulative value of all retransmission timeouts).
     * @return false if retransmissions are to continue.
     *        If the method returns true, the RPC layer will
     *        abort the retransmissions and return an
     *        InterruptedIOException to the application.
     */
    @Override
    public abstract boolean timeout(String server, int retries, int wait);

    /**
     * Called when a server reply is recieved after a timeout.
     *
     * @param server The name of the server that returned
     *        the reply.
     */
    @Override
    public abstract void ok(String server);
}

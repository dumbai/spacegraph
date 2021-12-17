/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package spacegraph.space2d.phys.collision.broadphase;

import jcog.math.v2;
import spacegraph.space2d.phys.callbacks.DebugDraw;
import spacegraph.space2d.phys.callbacks.PairCallback;
import spacegraph.space2d.phys.callbacks.TreeCallback;
import spacegraph.space2d.phys.callbacks.TreeRayCastCallback;
import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.collision.RayCastInput;

import java.util.Arrays;

/**
 * The broad-phase is used for computing pairs and performing volume queries and ray casts. This
 * broad-phase does not persist pairs. Instead, this reports potentially new pairs. It is up to the
 * client to consume the new pairs and to track subsequent overlap.
 *
 * @author Daniel Murphy
 */
public class DefaultBroadPhaseBuffer implements TreeCallback, BroadPhase {

    private final BroadPhaseStrategy m_tree;

    private int m_proxyCount;

    private int[] m_moveBuffer;
    private int m_moveCapacity;
    private int m_moveCount;

    private Pair[] m_pairBuffer;
    private int m_pairCapacity;
    private int m_pairCount;

    private int m_queryProxyId;

    public DefaultBroadPhaseBuffer(BroadPhaseStrategy strategy) {
        m_proxyCount = 0;

        m_pairCapacity = 16;
        m_pairCount = 0;
        m_pairBuffer = new Pair[m_pairCapacity];
        for (int i = 0; i < m_pairCapacity; i++) {
            m_pairBuffer[i] = new Pair();
        }

        m_moveCapacity = 16;
        m_moveCount = 0;
        m_moveBuffer = new int[m_moveCapacity];

        m_tree = strategy;
        m_queryProxyId = NULL_PROXY;
    }

    @Override
    public final int createProxy(AABB aabb, Object userData) {
        int proxyId = m_tree.createProxy(aabb, userData);
        ++m_proxyCount;
        bufferMove(proxyId);
        return proxyId;
    }

    @Override
    public final void destroyProxy(int proxyId) {
        unbufferMove(proxyId);
        --m_proxyCount;
        m_tree.destroyProxy(proxyId);
    }

    @Override
    public final void moveProxy(int proxyId, AABB aabb, v2 displacement) {
        boolean buffer = m_tree.moveProxy(proxyId, aabb, displacement);
        if (buffer) {
            bufferMove(proxyId);
        }
    }

    @Override
    public void touchProxy(int proxyId) {
        bufferMove(proxyId);
    }

    @Override
    public Object get(int proxyId) {
        return m_tree.getUserData(proxyId);
    }

    @Override
    public AABB getFatAABB(int proxyId) {
        return m_tree.getFatAABB(proxyId);
    }

    @Override
    public boolean testOverlap(int proxyIdA, int proxyIdB) {
        
        
        AABB a = m_tree.getFatAABB(proxyIdA);
        AABB b = m_tree.getFatAABB(proxyIdB);
        if (b.lowerBound.x - a.upperBound.x > 0.0f || b.lowerBound.y - a.upperBound.y > 0.0f) {
            return false;
        }

        return !(a.lowerBound.x - b.upperBound.x > 0.0f) && !(a.lowerBound.y - b.upperBound.y > 0.0f);
    }

    @Override
    public final int getProxyCount() {
        return m_proxyCount;
    }

    @Override
    public void drawTree(DebugDraw argDraw) {
        m_tree.drawTree(argDraw);
    }

    @Override
    public final void updatePairs(PairCallback callback) {
        
        m_pairCount = 0;

        
        for (int i = 0; i < m_moveCount; ++i) {
            m_queryProxyId = m_moveBuffer[i];
            if (m_queryProxyId == NULL_PROXY) {
                continue;
            }

            
            
            AABB fatAABB = m_tree.getFatAABB(m_queryProxyId);

            
            
            m_tree.query(this, fatAABB);
        }
        

        
        m_moveCount = 0;

        
        Arrays.sort(m_pairBuffer, 0, m_pairCount);

        
        int i = 0;
        while (i < m_pairCount) {
            Pair primaryPair = m_pairBuffer[i];
            Object userDataA = m_tree.getUserData(primaryPair.proxyIdA);
            Object userDataB = m_tree.getUserData(primaryPair.proxyIdB);

            
            callback.addPair(userDataA, userDataB);
            ++i;

            
            while (i < m_pairCount) {
                Pair pair = m_pairBuffer[i];
                if (pair.proxyIdA != primaryPair.proxyIdA || pair.proxyIdB != primaryPair.proxyIdB) {
                    break;
                }
                ++i;
            }
        }
    }

    @Override
    public final void query(TreeCallback callback, AABB aabb) {
        m_tree.query(callback, aabb);
    }

    @Override
    public final void raycast(TreeRayCastCallback callback, RayCastInput input) {
        m_tree.raycast(callback, input);
    }

    @Override
    public final int getTreeHeight() {
        return m_tree.getHeight();
    }

    @Override
    public int getTreeBalance() {
        return m_tree.getMaxBalance();
    }

    @Override
    public float getTreeQuality() {
        return m_tree.getAreaRatio();
    }

    private void bufferMove(int proxyId) {
        if (m_moveCount == m_moveCapacity) {
            int[] old = m_moveBuffer;
            m_moveCapacity *= 2;
            m_moveBuffer = new int[m_moveCapacity];
            System.arraycopy(old, 0, m_moveBuffer, 0, old.length);
        }

        m_moveBuffer[m_moveCount] = proxyId;
        ++m_moveCount;
    }

    private void unbufferMove(int proxyId) {
        for (int i = 0; i < m_moveCount; i++) {
            if (m_moveBuffer[i] == proxyId) {
                m_moveBuffer[i] = NULL_PROXY;
            }
        }
    }

    /**
     * This is called from DynamicTree::query when we are gathering pairs.
     */
    public final boolean treeCallback(int proxyId) {
        
        if (proxyId == m_queryProxyId) {
            return true;
        }

        
        if (m_pairCount == m_pairCapacity) {
            Pair[] oldBuffer = m_pairBuffer;
            m_pairCapacity *= 2;
            m_pairBuffer = new Pair[m_pairCapacity];
            System.arraycopy(oldBuffer, 0, m_pairBuffer, 0, oldBuffer.length);
            for (int i = oldBuffer.length; i < m_pairCapacity; i++) {
                m_pairBuffer[i] = new Pair();
            }
        }

        Pair thePair = m_pairBuffer[m_pairCount++];
        if (proxyId < m_queryProxyId) {
            thePair.proxyIdA = proxyId;
            thePair.proxyIdB = m_queryProxyId;
        } else {
            thePair.proxyIdA = m_queryProxyId;
            thePair.proxyIdB = proxyId;
        }

        return true;
    }
}

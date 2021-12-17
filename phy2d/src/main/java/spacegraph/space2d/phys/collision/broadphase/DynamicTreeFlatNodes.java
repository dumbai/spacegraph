/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
import spacegraph.space2d.phys.callbacks.TreeCallback;
import spacegraph.space2d.phys.callbacks.TreeRayCastCallback;
import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.collision.RayCastInput;
import spacegraph.space2d.phys.common.BufferUtils;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.util.math.Color3f;

public class DynamicTreeFlatNodes implements BroadPhaseStrategy {
    public static final int MAX_STACK_SIZE = 64;
    private static final int NULL_NODE = -1;
    public static final int INITIAL_BUFFER_LENGTH = 16;

    private int m_root;
    private AABB[] m_aabb;
    private Object[] m_userData;
    private int[] m_parent;
    private int[] m_child1;
    private int[] m_child2;
    private int[] m_height;

    private int m_nodeCount;
    private int m_nodeCapacity;

    private int m_freeList;

    private final v2[] drawVecs = new v2[4];

    public DynamicTreeFlatNodes() {
        m_root = NULL_NODE;
        m_nodeCount = 0;
        m_nodeCapacity = 16;
        expandBuffers(0, m_nodeCapacity);

        for (int i = 0; i < drawVecs.length; i++) {
            drawVecs[i] = new v2();
        }
    }

    private void expandBuffers(int oldSize, int newSize) {
        m_aabb = BufferUtils.reallocateBuffer(AABB.class, m_aabb, oldSize, newSize);
        m_userData = BufferUtils.reallocateBuffer(Object.class, m_userData, oldSize, newSize);
        m_parent = BufferUtils.reallocateBuffer(m_parent, oldSize, newSize);
        m_child1 = BufferUtils.reallocateBuffer(m_child1, oldSize, newSize);
        m_child2 = BufferUtils.reallocateBuffer(m_child2, oldSize, newSize);
        m_height = BufferUtils.reallocateBuffer(m_height, oldSize, newSize);

        
        for (int i = oldSize; i < newSize; i++) {
            m_aabb[i] = new AABB();
            m_parent[i] = (i == newSize - 1) ? NULL_NODE : i + 1;
            m_height[i] = -1;
            m_child1[i] = -1;
            m_child2[i] = -1;
        }
        m_freeList = oldSize;
    }

    @Override
    public final int createProxy(AABB aabb, Object userData) {
        int node = allocateNode();
        
        AABB nodeAABB = m_aabb[node];
        nodeAABB.lowerBound.x = aabb.lowerBound.x - Settings.aabbExtension;
        nodeAABB.lowerBound.y = aabb.lowerBound.y - Settings.aabbExtension;
        nodeAABB.upperBound.x = aabb.upperBound.x + Settings.aabbExtension;
        nodeAABB.upperBound.y = aabb.upperBound.y + Settings.aabbExtension;
        m_userData[node] = userData;

        insertLeaf(node);

        return node;
    }

    @Override
    public final void destroyProxy(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        assert (m_child1[proxyId] == NULL_NODE);

        removeLeaf(proxyId);
        freeNode(proxyId);
    }

    @Override
    public final boolean moveProxy(int proxyId, AABB aabb, v2 displacement) {
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        int node = proxyId;
        assert (m_child1[node] == NULL_NODE);

        AABB nodeAABB = m_aabb[node];
        
        if (nodeAABB.lowerBound.x <= aabb.lowerBound.x && nodeAABB.lowerBound.y <= aabb.lowerBound.y
                && aabb.upperBound.x <= nodeAABB.upperBound.x && aabb.upperBound.y <= nodeAABB.upperBound.y) {
            return false;
        }

        removeLeaf(node);

        
        v2 lowerBound = nodeAABB.lowerBound;
        v2 upperBound = nodeAABB.upperBound;
        lowerBound.x = aabb.lowerBound.x - Settings.aabbExtension;
        lowerBound.y = aabb.lowerBound.y - Settings.aabbExtension;
        upperBound.x = aabb.upperBound.x + Settings.aabbExtension;
        upperBound.y = aabb.upperBound.y + Settings.aabbExtension;

        
        float dx = displacement.x * Settings.aabbMultiplier;
        float dy = displacement.y * Settings.aabbMultiplier;
        if (dx < 0.0f) {
            lowerBound.x += dx;
        } else {
            upperBound.x += dx;
        }

        if (dy < 0.0f) {
            lowerBound.y += dy;
        } else {
            upperBound.y += dy;
        }

        insertLeaf(proxyId);
        return true;
    }

    @Override
    public final Object getUserData(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCount);
        return m_userData[proxyId];
    }

    @Override
    public final AABB getFatAABB(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCount);
        return m_aabb[proxyId];
    }

    private int[] nodeStack = new int[20];
    private int nodeStackIndex;

    @Override
    public final void query(TreeCallback callback, AABB aabb) {
        nodeStackIndex = 0;
        nodeStack[nodeStackIndex++] = m_root;

        while (nodeStackIndex > 0) {
            int node = nodeStack[--nodeStackIndex];
            if (node == NULL_NODE) {
                continue;
            }

            if (AABB.testOverlap(m_aabb[node], aabb)) {
                int child1 = m_child1[node];
                if (child1 == NULL_NODE) {
                    boolean proceed = callback.treeCallback(node);
                    if (!proceed) {
                        return;
                    }
                } else {
                    if (nodeStack.length - nodeStackIndex - 2 <= 0) {
                        nodeStack =
                                BufferUtils.reallocateBuffer(nodeStack, nodeStack.length, nodeStack.length * 2);
                    }
                    nodeStack[nodeStackIndex++] = child1;
                    nodeStack[nodeStackIndex++] = m_child2[node];
                }
            }
        }
    }

    private final v2 r = new v2();
    private final AABB aabb = new AABB();
    private final RayCastInput subInput = new RayCastInput();

    @Override
    public void raycast(TreeRayCastCallback callback, RayCastInput input) {
        v2 p1 = input.p1;
        v2 p2 = input.p2;
        float p1x = p1.x, p2x = p2.x, p1y = p1.y, p2y = p2.y;
        r.x = p2x - p1x;
        r.y = p2y - p1y;
        assert ((r.x * r.x + r.y * r.y) > 0.0f);
        r.normalize();
        float rx = r.x;
        float ry = r.y;


        float maxFraction = input.maxFraction;

        
        AABB segAABB = aabb;


        float tempx = (p2x - p1x) * maxFraction + p1x;
        float tempy = (p2y - p1y) * maxFraction + p1y;
        segAABB.lowerBound.x = Math.min(p1x, tempx);
        segAABB.lowerBound.y = Math.min(p1y, tempy);
        segAABB.upperBound.x = Math.max(p1x, tempx);
        segAABB.upperBound.y = Math.max(p1y, tempy);
        

        nodeStackIndex = 0;
        nodeStack[nodeStackIndex++] = m_root;
        float vy = 1.0f * rx;
        float absVy = Math.abs(vy);
        float vx = -1.0f * ry;
        float absVx = Math.abs(vx);
        while (nodeStackIndex > 0) {
            int node = nodeStack[--nodeStackIndex] = m_root;
            if (node == NULL_NODE) {
                continue;
            }

            AABB nodeAABB = m_aabb[node];
            if (!AABB.testOverlap(nodeAABB, segAABB)) {
                continue;
            }


            float cx = (nodeAABB.lowerBound.x + nodeAABB.upperBound.x) * 0.5f;
            float cy = (nodeAABB.lowerBound.y + nodeAABB.upperBound.y) * 0.5f;
            float hx = (nodeAABB.upperBound.x - nodeAABB.lowerBound.x) * 0.5f;
            float hy = (nodeAABB.upperBound.y - nodeAABB.lowerBound.y) * 0.5f;
            tempx = p1x - cx;
            tempy = p1y - cy;
            float separation = Math.abs(vx * tempx + vy * tempy) - (absVx * hx + absVy * hy);
            if (separation > 0.0f) {
                continue;
            }

            int child1 = m_child1[node];
            if (child1 == NULL_NODE) {
                subInput.p1.x = p1x;
                subInput.p1.y = p1y;
                subInput.p2.x = p2x;
                subInput.p2.y = p2y;
                subInput.maxFraction = maxFraction;

                float value = callback.raycastCallback(subInput, node);

                if (value == 0.0f) {
                    
                    return;
                }

                if (value > 0.0f) {
                    
                    maxFraction = value;
                    
                    
                    
                    tempx = (p2x - p1x) * maxFraction + p1x;
                    tempy = (p2y - p1y) * maxFraction + p1y;
                    segAABB.lowerBound.x = Math.min(p1x, tempx);
                    segAABB.lowerBound.y = Math.min(p1y, tempy);
                    segAABB.upperBound.x = Math.max(p1x, tempx);
                    segAABB.upperBound.y = Math.max(p1y, tempy);
                }
            } else {
                nodeStack[nodeStackIndex++] = child1;
                nodeStack[nodeStackIndex++] = m_child2[node];
            }
        }
    }

    @Override
    public final int computeHeight() {
        return computeHeight(m_root);
    }

    private int computeHeight(int node) {
        assert (0 <= node && node < m_nodeCapacity);

        if (m_child1[node] == NULL_NODE) {
            return 0;
        }
        int height1 = computeHeight(m_child1[node]);
        int height2 = computeHeight(m_child2[node]);
        return 1 + Math.max(height1, height2);
    }

    /**
     * Validate this tree. For testing.
     */
    public void validate() {
        validateStructure(m_root);
        validateMetrics(m_root);

        int freeCount = 0;
        int freeNode = m_freeList;
        while (freeNode != NULL_NODE) {
            assert (0 <= freeNode && freeNode < m_nodeCapacity);
            freeNode = m_parent[freeNode];
            ++freeCount;
        }

        assert (getHeight() == computeHeight());
        assert (m_nodeCount + freeCount == m_nodeCapacity);
    }

    @Override
    public int getHeight() {
        if (m_root == NULL_NODE) {
            return 0;
        }
        return m_height[m_root];
    }

    @Override
    public int getMaxBalance() {
        int maxBalance = 0;
        for (int i = 0; i < m_nodeCapacity; ++i) {
            if (m_height[i] <= 1) {
                continue;
            }

            assert (m_child1[i] != NULL_NODE);

            int child1 = m_child1[i];
            int child2 = m_child2[i];
            int balance = Math.abs(m_height[child2] - m_height[child1]);
            maxBalance = Math.max(maxBalance, balance);
        }

        return maxBalance;
    }

    @Override
    public float getAreaRatio() {
        if (m_root == NULL_NODE) {
            return 0.0f;
        }

        int root = m_root;
        float rootArea = m_aabb[root].getPerimeter();

        float totalArea = 0.0f;
        for (int i = 0; i < m_nodeCapacity; ++i) {
            if (m_height[i] < 0) {
                
                continue;
            }

            totalArea += m_aabb[i].getPerimeter();
        }

        return totalArea / rootArea;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    private int allocateNode() {
        if (m_freeList == NULL_NODE) {
            assert (m_nodeCount == m_nodeCapacity);
            m_nodeCapacity *= 2;
            expandBuffers(m_nodeCount, m_nodeCapacity);
        }
        assert (m_freeList != NULL_NODE);
        int node = m_freeList;
        m_freeList = m_parent[node];
        m_parent[node] = NULL_NODE;
        m_child1[node] = NULL_NODE;
        m_height[node] = 0;
        ++m_nodeCount;
        return node;
    }

    /**
     * returns a node to the pool
     */
    private void freeNode(int node) {
        assert (node != NULL_NODE);
        assert (0 < m_nodeCount);
        m_parent[node] = m_freeList;
        m_height[node] = -1;
        m_freeList = node;
        m_nodeCount--;
    }

    private final AABB combinedAABB = new AABB();

    private void insertLeaf(int leaf) {
        if (m_root == NULL_NODE) {
            m_root = leaf;
            m_parent[m_root] = NULL_NODE;
            return;
        }

        
        AABB leafAABB = m_aabb[leaf];
        int index = m_root;
        while (m_child1[index] != NULL_NODE) {
            int node = index;
            int child1 = m_child1[node];
            int child2 = m_child2[node];
            AABB nodeAABB = m_aabb[node];
            float area = nodeAABB.getPerimeter();

            combinedAABB.combine(nodeAABB, leafAABB);
            float combinedArea = combinedAABB.getPerimeter();


            float inheritanceCost = 2.0f * (combinedArea - area);

            
            float cost1;
            AABB child1AABB = m_aabb[child1];
            if (m_child1[child1] == NULL_NODE) {
                combinedAABB.combine(leafAABB, child1AABB);
                cost1 = combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                combinedAABB.combine(leafAABB, child1AABB);
                float oldArea = child1AABB.getPerimeter();
                float newArea = combinedAABB.getPerimeter();
                cost1 = (newArea - oldArea) + inheritanceCost;
            }

            
            float cost2;
            AABB child2AABB = m_aabb[child2];
            if (m_child1[child2] == NULL_NODE) {
                combinedAABB.combine(leafAABB, child2AABB);
                cost2 = combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                combinedAABB.combine(leafAABB, child2AABB);
                float oldArea = child2AABB.getPerimeter();
                float newArea = combinedAABB.getPerimeter();
                cost2 = newArea - oldArea + inheritanceCost;
            }


            float cost = 2.0f * combinedArea;
            if (cost < cost1 && cost < cost2) {
                break;
            }


			index = cost1 < cost2 ? child1 : child2;
        }

        int sibling = index;
        int oldParent = m_parent[sibling];
        int newParent = allocateNode();
        m_parent[newParent] = oldParent;
        m_userData[newParent] = null;
        m_aabb[newParent].combine(leafAABB, m_aabb[sibling]);
        m_height[newParent] = m_height[sibling] + 1;

        if (oldParent != NULL_NODE) {
            
            if (m_child1[oldParent] == sibling) {
                m_child1[oldParent] = newParent;
            } else {
                m_child2[oldParent] = newParent;
            }

            m_child1[newParent] = sibling;
            m_child2[newParent] = leaf;
            m_parent[sibling] = newParent;
            m_parent[leaf] = newParent;
        } else {
            
            m_child1[newParent] = sibling;
            m_child2[newParent] = leaf;
            m_parent[sibling] = newParent;
            m_parent[leaf] = newParent;
            m_root = newParent;
        }

        
        index = m_parent[leaf];
        while (index != NULL_NODE) {
            index = balance(index);

            int child1 = m_child1[index];
            int child2 = m_child2[index];

            assert (child1 != NULL_NODE);
            assert (child2 != NULL_NODE);

            m_height[index] = 1 + Math.max(m_height[child1], m_height[child2]);
            m_aabb[index].combine(m_aabb[child1], m_aabb[child2]);

            index = m_parent[index];
        }
        
    }

    private void removeLeaf(int leaf) {
        if (leaf == m_root) {
            m_root = NULL_NODE;
            return;
        }

        int parent = m_parent[leaf];
        int grandParent = m_parent[parent];
        int parentChild1 = m_child1[parent];
        int parentChild2 = m_child2[parent];
        int sibling;
		sibling = parentChild1 == leaf ? parentChild2 : parentChild1;

        if (grandParent != NULL_NODE) {
            
            if (m_child1[grandParent] == parent) {
                m_child1[grandParent] = sibling;
            } else {
                m_child2[grandParent] = sibling;
            }
            m_parent[sibling] = grandParent;
            freeNode(parent);

            
            int index = grandParent;
            while (index != NULL_NODE) {
                index = balance(index);

                int child1 = m_child1[index];
                int child2 = m_child2[index];

                m_aabb[index].combine(m_aabb[child1], m_aabb[child2]);
                m_height[index] = 1 + Math.max(m_height[child1], m_height[child2]);

                index = m_parent[index];
            }
        } else {
            m_root = sibling;
            m_parent[sibling] = NULL_NODE;
            freeNode(parent);
        }

        
    }

    
    
    private int balance(int iA) {
        assert (iA != NULL_NODE);

        int A = iA;
        if (m_child1[A] == NULL_NODE || m_height[A] < 2) {
            return iA;
        }

        int iB = m_child1[A];
        int iC = m_child2[A];
        assert (0 <= iB && iB < m_nodeCapacity);
        assert (0 <= iC && iC < m_nodeCapacity);

        int B = iB;
        int C = iC;

        int balance = m_height[C] - m_height[B];

        
        if (balance > 1) {
            int iF = m_child1[C];
            int iG = m_child2[C];


            assert (0 <= iF && iF < m_nodeCapacity);
            assert (0 <= iG && iG < m_nodeCapacity);

            
            m_child1[C] = iA;
            int cParent = m_parent[C] = m_parent[A];
            m_parent[A] = iC;

            
            if (cParent != NULL_NODE) {
                if (m_child1[cParent] == iA) {
                    m_child1[cParent] = iC;
                } else {
                    assert (m_child2[cParent] == iA);
                    m_child2[cParent] = iC;
                }
            } else {
                m_root = iC;
            }


            int G = iG;
            int F = iF;
            if (m_height[F] > m_height[G]) {
                m_child2[C] = iF;
                m_child2[A] = iG;
                m_parent[G] = iA;
                m_aabb[A].combine(m_aabb[B], m_aabb[G]);
                m_aabb[C].combine(m_aabb[A], m_aabb[F]);

                m_height[A] = 1 + Math.max(m_height[B], m_height[G]);
                m_height[C] = 1 + Math.max(m_height[A], m_height[F]);
            } else {
                m_child2[C] = iG;
                m_child2[A] = iF;
                m_parent[F] = iA;
                m_aabb[A].combine(m_aabb[B], m_aabb[F]);
                m_aabb[C].combine(m_aabb[A], m_aabb[G]);

                m_height[A] = 1 + Math.max(m_height[B], m_height[F]);
                m_height[C] = 1 + Math.max(m_height[A], m_height[G]);
            }

            return iC;
        }

        
        if (balance < -1) {
            int iD = m_child1[B];
            int iE = m_child2[B];
            assert (0 <= iD && iD < m_nodeCapacity);
            assert (0 <= iE && iE < m_nodeCapacity);

            
            m_child1[B] = iA;
            int Bparent = m_parent[B] = m_parent[A];
            m_parent[A] = iB;

            
            if (Bparent != NULL_NODE) {
                if (m_child1[Bparent] == iA) {
                    m_child1[Bparent] = iB;
                } else {
                    assert (m_child2[Bparent] == iA);
                    m_child2[Bparent] = iB;
                }
            } else {
                m_root = iB;
            }


            int E = iE;
            int D = iD;
            if (m_height[D] > m_height[E]) {
                m_child2[B] = iD;
                m_child1[A] = iE;
                m_parent[E] = iA;
                m_aabb[A].combine(m_aabb[C], m_aabb[E]);
                m_aabb[B].combine(m_aabb[A], m_aabb[D]);

                m_height[A] = 1 + Math.max(m_height[C], m_height[E]);
                m_height[B] = 1 + Math.max(m_height[A], m_height[D]);
            } else {
                m_child2[B] = iE;
                m_child1[A] = iD;
                m_parent[D] = iA;
                m_aabb[A].combine(m_aabb[C], m_aabb[D]);
                m_aabb[B].combine(m_aabb[A], m_aabb[E]);

                m_height[A] = 1 + Math.max(m_height[C], m_height[D]);
                m_height[B] = 1 + Math.max(m_height[A], m_height[E]);
            }

            return iB;
        }

        return iA;
    }

    private void validateStructure(int node) {
        if (node == NULL_NODE) {
            return;
        }

        assert node != m_root || (m_parent[node] == NULL_NODE);

        int child1 = m_child1[node];
        int child2 = m_child2[node];

        if (child1 == NULL_NODE) {
            //assert (child1 == NULL_NODE);
            //assert (child2 == NULL_NODE);
            assert (m_height[node] == 0);
            return;
        }

        assert (0 <= child1 && child1 < m_nodeCapacity);
        assert (0 <= child2 && child2 < m_nodeCapacity);

        assert (m_parent[child1] == node);
        assert (m_parent[child2] == node);

        validateStructure(child1);
        validateStructure(child2);
    }

    private void validateMetrics(int node) {
        if (node == NULL_NODE) {
            return;
        }

        int child1 = m_child1[node];
        int child2 = m_child2[node];

        if (child1 == NULL_NODE) {
            //assert (child1 == NULL_NODE);
            assert (child2 == NULL_NODE);
            assert (m_height[node] == 0);
            return;
        }

        assert (0 <= child1 && child1 < m_nodeCapacity);
        assert (child2 != child1 && 0 <= child2 && child2 < m_nodeCapacity);

        int height1 = m_height[child1];
        int height2 = m_height[child2];
        int height = 1 + Math.max(height1, height2);
        assert (m_height[node] == height);

        AABB aabb = new AABB();
        aabb.combine(m_aabb[child1], m_aabb[child2]);

        assert (aabb.lowerBound.equals(m_aabb[node].lowerBound));
        assert (aabb.upperBound.equals(m_aabb[node].upperBound));

        validateMetrics(child1);
        validateMetrics(child2);
    }

    @Override
    public void drawTree(DebugDraw argDraw) {
        if (m_root == NULL_NODE) {
            return;
        }
        int height = computeHeight();
        drawTree(argDraw, m_root, 0, height);
    }

    private final Color3f color = new Color3f();
    private final v2 textVec = new v2();

    private void drawTree(DebugDraw argDraw, int node, int spot, int height) {
        AABB a = m_aabb[node];
        a.vertices(drawVecs);

        color.set(1, (height - spot) * 1.0f / height, (height - spot) * 1.0f / height);
        argDraw.drawPolygon(drawVecs, 4, color);

        argDraw.getViewportTranform().getWorldToScreen(a.upperBound, textVec);
        argDraw.drawString(textVec.x, textVec.y, node + "-" + (spot + 1) + '/' + height, color);

        int c1 = m_child1[node];
        int c2 = m_child2[node];
        if (c1 != NULL_NODE) {
            drawTree(argDraw, c1, spot + 1, height);
        }
        if (c2 != NULL_NODE) {
            drawTree(argDraw, c2, spot + 1, height);
        }
    }
}
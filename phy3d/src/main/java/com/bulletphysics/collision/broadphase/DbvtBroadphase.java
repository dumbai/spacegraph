/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

// Dbvt implementation by Nathanael Presson

package com.bulletphysics.collision.broadphase;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author jezek2
 */
public class DbvtBroadphase extends BroadphaseInterface {

    public static final int DYNAMIC_SET = 0; // Dynamic set index
    public static final int FIXED_SET = 1; // Fixed set index
    private static final float DBVT_BP_MARGIN = 0.05f;
    private static final int STAGECOUNT = 2; // Number of stages
    public final OverlappingPairCache paircache;                         // Pair cache
    private final Dbvt[] sets = new Dbvt[2];                        // Dbvt sets
    private final DbvtProxy[] stageRoots = new DbvtProxy[STAGECOUNT + 1]; // Stages list
    private final float predictedframes;                                  // Frames predicted
    private final int fupdates;                                           // % of fixed updates per frame
    private final int dupdates;                                           // % of dynamic updates per frame
    private final boolean releasepaircache;                               // Release pair cache on delete
    private int stageCurrent;                                       // Current stage
    private int pid;                                                // Parse id
    private int gid;                                                // Gen id

    //#if DBVT_BP_PROFILE
    //btClock					m_clock;
    //struct	{
    //		unsigned long		m_total;
    //		unsigned long		m_ddcollide;
    //		unsigned long		m_fdcollide;
    //		unsigned long		m_cleanup;
    //		unsigned long		m_jobcount;
    //		}				m_profiling;
    //#endif

    public DbvtBroadphase() {
        this(null);
    }

    private DbvtBroadphase(OverlappingPairCache paircache) {
        sets[0] = new Dbvt();
        sets[1] = new Dbvt();

        //Dbvt.benchmark();
        releasepaircache = (paircache == null);
        predictedframes = 2;
        stageCurrent = 0;
        fupdates = 1;
        dupdates = 1;
        this.paircache = (paircache != null ? paircache : new HashedOverlappingPairCache());
        gid = 0;
        pid = 0;
    }

    private static DbvtProxy listappend(DbvtProxy item, DbvtProxy list) {
        DbvtProxy[] itemLinks = item.links;
        itemLinks[0] = null;
        itemLinks[1] = list;
        if (list != null) list.links[0] = item;
        return item;
    }

    private static DbvtProxy listremove(DbvtProxy item, DbvtProxy list) {
        DbvtProxy[] itemLinks = item.links;
        if (itemLinks[0] != null) {
            itemLinks[0].links[1] = itemLinks[1];
        } else {
            list = itemLinks[1];
        }

        if (itemLinks[1] != null) {
            itemLinks[1].links[0] = itemLinks[0];
        }
        return list;
    }

    private void collide(Dispatcher dispatcher) {
        //SPC(m_profiling.m_total);

        // optimize:
        Dbvt s0 = sets[0], s1 = sets[1];
        s0.optimizeIncremental(1 + (s0.leaves.get() * dupdates) / 100);
        s1.optimizeIncremental(1 + (s1.leaves.get() * fupdates) / 100);

        // dynamic -> fixed set:
        stageCurrent = (stageCurrent + 1) % STAGECOUNT;
        DbvtProxy current = stageRoots[stageCurrent];
        Dbvt.Node s1r = s1.root;
        if (current != null) {
            DbvtTreeCollider collider = new DbvtTreeCollider(this);
            do {
                DbvtProxy next = current.links[1];
                stageRoots[current.stage] = listremove(current, stageRoots[current.stage]);
                stageRoots[STAGECOUNT] = listappend(current, stageRoots[STAGECOUNT]);
                Dbvt.collideTT(s1r, current.leaf, collider);
                s0.remove(current.leaf);
                current.leaf = s1.put(current, current.aabb);
                current.stage = STAGECOUNT;
                current = next;
            } while (current != null);
        }

        // collide dynamics:
        DbvtTreeCollider collider = new DbvtTreeCollider(this);
        Dbvt.Node s0r = s0.root;
        {
            //SPC(m_profiling.m_fdcollide);
            Dbvt.collideTT(s0r, s1r, collider);
        }
        {
            //SPC(m_profiling.m_ddcollide);
            Dbvt.collideTT(s0r, s0r, collider);
        }

        // clean up:
        {
            //SPC(m_profiling.m_cleanup);
            List<BroadphasePair> pairs = paircache.getOverlappingPairArray();
            if (!pairs.isEmpty()) {
                for (int i = 0, ni = pairs.size(); i < ni; i++) {
                    BroadphasePair p = pairs.get(i);
                    DbvtProxy pa = (DbvtProxy) p.pProxy0;
                    DbvtProxy pb = (DbvtProxy) p.pProxy1;
                    if (!DbvtAabbMm.intersects(pa.aabb, pb.aabb)) {
                        //if(pa>pb) btSwap(pa,pb);
                        if (pa.hashCode() > pb.hashCode()) {
                            DbvtProxy tmp = pa;
                            pa = pb;
                            pb = tmp;
                        }
                        paircache.removeOverlappingPair(pa, pb, dispatcher);
                        ni--;
                        i--;
                    }
                }
            }
        }
        pid++;
    }

    public BroadphaseProxy createProxy(Vector3f aabbMin, Vector3f aabbMax, BroadphaseNativeType shapeType, Object userPtr, short collisionFilterGroup, short collisionFilterMask, Dispatcher dispatcher, Object multiSapProxy) {
        DbvtProxy proxy = new DbvtProxy(userPtr, collisionFilterGroup, collisionFilterMask);
        DbvtAabbMm.fromMinMax(aabbMin, aabbMax, proxy.aabb);
        proxy.leaf = sets[0].put(proxy, proxy.aabb);
        proxy.stage = stageCurrent;
        proxy.uniqueId = ++gid;
        stageRoots[stageCurrent] = listappend(proxy, stageRoots[stageCurrent]);
        return (proxy);
    }

    public void destroyProxy(BroadphaseProxy absproxy, Dispatcher dispatcher) {
        DbvtProxy proxy = (DbvtProxy) absproxy;
        int stage = proxy.stage;
        sets[stage == STAGECOUNT ? 1 : 0].remove(proxy.leaf);
        stageRoots[stage] = listremove(proxy, stageRoots[stage]);
        paircache.removeOverlappingPairsContainingProxy(proxy, dispatcher);
        //btAlignedFree(proxy);
    }

    public void setAabb(BroadphaseProxy absproxy, Vector3f aabbMin, Vector3f aabbMax, Dispatcher dispatcher) {
        DbvtProxy proxy = (DbvtProxy) absproxy;
        DbvtAabbMm aabb = DbvtAabbMm.fromMinMax(aabbMin, aabbMax, new DbvtAabbMm());
        if (proxy.stage == STAGECOUNT) {
            // fixed -> dynamic set
            sets[1].remove(proxy.leaf);
            proxy.leaf = sets[0].put(proxy, aabb);
        } else {
            // dynamic set:
            if (DbvtAabbMm.intersects(proxy.leaf.volume, aabb)) {/* Moving				*/
                Vector3f delta = new Vector3f();
                delta.add(aabbMin, aabbMax);
                delta.scale(0.5f);
                delta.sub(proxy.aabb.center(new Vector3f()));
                //#ifdef DBVT_BP_MARGIN
                delta.scale(predictedframes);
                sets[0].update(proxy.leaf, aabb, delta, DBVT_BP_MARGIN);
                //#else
                //m_sets[0].update(proxy->leaf,aabb,delta*m_predictedframes);
                //#endif
            } else {
                // teleporting:
                sets[0].update(proxy.leaf, aabb);
            }
        }

        stageRoots[proxy.stage] = listremove(proxy, stageRoots[proxy.stage]);
        proxy.aabb.set(aabb);
        proxy.stage = stageCurrent;
        stageRoots[stageCurrent] = listappend(proxy, stageRoots[stageCurrent]);
    }

    public void calculateOverlappingPairs(Dispatcher dispatcher) {
        collide(dispatcher);

        //#if DBVT_BP_PROFILE
        //if(0==(m_pid%DBVT_BP_PROFILING_RATE))
        //	{
        //	printf("fixed(%u) dynamics(%u) pairs(%u)\r\n",m_sets[1].m_leafs,m_sets[0].m_leafs,m_paircache->getNumOverlappingPairs());
        //	printf("mode:    %s\r\n",m_mode==MODE_FULL?"full":"incremental");
        //	printf("cleanup: %s\r\n",m_cleanupmode==CLEANUP_FULL?"full":"incremental");
        //	unsigned int	total=m_profiling.m_total;
        //	if(total<=0) total=1;
        //	printf("ddcollide: %u%% (%uus)\r\n",(50+m_profiling.m_ddcollide*100)/total,m_profiling.m_ddcollide/DBVT_BP_PROFILING_RATE);
        //	printf("fdcollide: %u%% (%uus)\r\n",(50+m_profiling.m_fdcollide*100)/total,m_profiling.m_fdcollide/DBVT_BP_PROFILING_RATE);
        //	printf("cleanup:   %u%% (%uus)\r\n",(50+m_profiling.m_cleanup*100)/total,m_profiling.m_cleanup/DBVT_BP_PROFILING_RATE);
        //	printf("total:     %uus\r\n",total/DBVT_BP_PROFILING_RATE);
        //	const unsigned long	sum=m_profiling.m_ddcollide+
        //							m_profiling.m_fdcollide+
        //							m_profiling.m_cleanup;
        //	printf("leaked: %u%% (%uus)\r\n",100-((50+sum*100)/total),(total-sum)/DBVT_BP_PROFILING_RATE);
        //	printf("job counts: %u%%\r\n",(m_profiling.m_jobcount*100)/((m_sets[0].m_leafs+m_sets[1].m_leafs)*DBVT_BP_PROFILING_RATE));
        //	clear(m_profiling);
        //	m_clock.reset();
        //	}
        //#endif
    }

    public OverlappingPairCache getOverlappingPairCache() {
        return paircache;
    }

    public void getBroadphaseAabb(Vector3f aabbMin, Vector3f aabbMax) {
        DbvtAabbMm bounds = new DbvtAabbMm();
		Dbvt s0 = sets[0];
		Dbvt s1 = sets[1];
		if (!s0.empty()) {
            if (!s1.empty()) {
                DbvtAabbMm.merge(s0.root.volume, s1.root.volume, bounds);
            } else {
                bounds.set(s0.root.volume);
            }
        } else if (!s1.empty()) {
            bounds.set(s1.root.volume);
        } else {
            DbvtAabbMm.fromRadius(new Vector3f(), 0.0f, bounds);
        }
        aabbMin.set(bounds.min);
        aabbMax.set(bounds.max);
    }

}
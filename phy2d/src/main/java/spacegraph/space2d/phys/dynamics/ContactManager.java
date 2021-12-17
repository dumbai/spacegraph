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
package spacegraph.space2d.phys.dynamics;

import spacegraph.space2d.phys.callbacks.ContactFilter;
import spacegraph.space2d.phys.callbacks.ContactListener;
import spacegraph.space2d.phys.callbacks.PairCallback;
import spacegraph.space2d.phys.collision.broadphase.BroadPhase;
import spacegraph.space2d.phys.collision.shapes.ShapeType;
import spacegraph.space2d.phys.dynamics.contacts.Contact;
import spacegraph.space2d.phys.dynamics.contacts.ContactEdge;
import spacegraph.space2d.phys.dynamics.contacts.ContactRegister;
import spacegraph.space2d.phys.fracture.FractureListener;
import spacegraph.space2d.phys.pooling.IDynamicStack;
import spacegraph.space2d.phys.pooling.IWorldPool;

/**
 * Delegate of World.
 *
 * @author Daniel Murphy
 */
public class ContactManager implements PairCallback {

    public final BroadPhase broadPhase;
    public Contact m_contactList;
    public int m_contactCount;
    public ContactListener contactListener;
    public FractureListener m_fractureListener;


    private final ContactRegister[][] contactStacks =
            new ContactRegister[ShapeType.values().length][ShapeType.values().length];

    
    public ContactManager(Dynamics2D dyn, BroadPhase broadPhase) {
        m_contactList = null;
        m_contactCount = 0;
        contactListener = null;
        this.broadPhase = broadPhase;

        IWorldPool pool = dyn.pool;
        addType(pool.getCircleContactStack(), ShapeType.CIRCLE, ShapeType.CIRCLE);
        addType(pool.getPolyCircleContactStack(), ShapeType.POLYGON, ShapeType.CIRCLE);
        addType(pool.getPolyContactStack(), ShapeType.POLYGON, ShapeType.POLYGON);
        addType(pool.getEdgeCircleContactStack(), ShapeType.EDGE, ShapeType.CIRCLE);
        addType(pool.getEdgePolyContactStack(), ShapeType.EDGE, ShapeType.POLYGON);
        addType(pool.getChainCircleContactStack(), ShapeType.CHAIN, ShapeType.CIRCLE);
        addType(pool.getChainPolyContactStack(), ShapeType.CHAIN, ShapeType.POLYGON);
    }
    private void addType(IDynamicStack<Contact> creator, ShapeType type1, ShapeType type2) {
        ContactRegister register = new ContactRegister();
        register.creator = creator;
        register.primary = true;
        contactStacks[type1.ordinal()][type2.ordinal()] = register;

        if (type1 != type2) {
            ContactRegister register2 = new ContactRegister();
            register2.creator = creator;
            register2.primary = false;
            contactStacks[type2.ordinal()][type1.ordinal()] = register2;
        }
    }
    /**
     * Broad-phase callback.
     *
     * @param proxyUserDataA
     * @param proxyUserDataB
     */
    public void addPair(Object proxyUserDataA, Object proxyUserDataB) {

        FixtureProxy proxyA = (FixtureProxy) proxyUserDataA;
        Fixture fixtureA = proxyA.fixture;
        if (fixtureA == null)
            return; 

        FixtureProxy proxyB = (FixtureProxy) proxyUserDataB;
        Fixture fixtureB = proxyB.fixture;
        if (fixtureB == null)
            return; 

        int indexA = proxyA.childIndex;
        int indexB = proxyB.childIndex;

        Body2D bodyA = fixtureA.getBody();
        Body2D bodyB = fixtureB.getBody();

        
        if (bodyA == bodyB || !bodyB.colllide(bodyA)) {
            return;
        }


        
        
        
        ContactEdge edge = bodyB.contacts();
        while (edge != null) {
            if (edge.other == bodyA) {
                Contact ec = edge.contact;
                Fixture fA = ec.aFixture;
                Fixture fB = ec.bFixture;
                int iA = ec.aIndex;
                int iB = ec.bIndex;

                if (fA == fixtureA && iA == indexA && fB == fixtureB && iB == indexB) {
                    
                    return;
                }

                if (fA == fixtureB && iA == indexB && fB == fixtureA && iB == indexA) {
                    
                    return;
                }
            }

            edge = edge.next;
        }


        
        if (!ContactFilter.shouldCollide(fixtureA, fixtureB)) {
            return;
        }

        
        Contact c = popContact(fixtureA, indexA, fixtureB, indexB);
        if (c == null) {
            return;
        }

        
        fixtureA = c.aFixture;
        fixtureB = c.bFixture;


        bodyA = fixtureA.getBody();
        bodyB = fixtureB.getBody();

        
        c.m_prev = null;
        c.m_next = m_contactList;
        if (m_contactList != null) {
            m_contactList.m_prev = c;
        }
        m_contactList = c;

        

        
        c.m_nodeA.contact = c;
        c.m_nodeA.other = bodyB;

        c.m_nodeA.prev = null;
        c.m_nodeA.next = bodyA.contacts;
        if (bodyA.contacts != null) {
            bodyA.contacts.prev = c.m_nodeA;
        }
        bodyA.contacts = c.m_nodeA;

        
        c.m_nodeB.contact = c;
        c.m_nodeB.other = bodyA;

        c.m_nodeB.prev = null;
        c.m_nodeB.next = bodyB.contacts;
        if (bodyB.contacts != null) {
            bodyB.contacts.prev = c.m_nodeB;
        }
        bodyB.contacts = c.m_nodeB;

        
        if (!fixtureA.isSensor() && !fixtureB.isSensor()) {
            bodyA.setAwake(true);
            bodyB.setAwake(true);
        }

        ++m_contactCount;
    }

    public void findNewContacts() {
        broadPhase.updatePairs(this);
    }

    public void destroy(Contact c) {
        Fixture a = c.aFixture;
        Fixture b = c.bFixture;
        Body2D aa = a.getBody();
        Body2D bb = b.getBody();

        if (contactListener != null && c.isTouching()) {
            contactListener.endContact(c);
        }

        
        if (c.m_prev != null) {
            c.m_prev.m_next = c.m_next;
        }

        if (c.m_next != null) {
            c.m_next.m_prev = c.m_prev;
        }

        if (c == m_contactList) {
            m_contactList = c.m_next;
        }

        
        if (c.m_nodeA.prev != null) {
            c.m_nodeA.prev.next = c.m_nodeA.next;
        }

        if (c.m_nodeA.next != null) {
            c.m_nodeA.next.prev = c.m_nodeA.prev;
        }

        if (c.m_nodeA == aa.contacts) {
            aa.contacts = c.m_nodeA.next;
        }

        
        if (c.m_nodeB.prev != null) {
            c.m_nodeB.prev.next = c.m_nodeB.next;
        }

        if (c.m_nodeB.next != null) {
            c.m_nodeB.next.prev = c.m_nodeB.prev;
        }

        if (c.m_nodeB == bb.contacts) {
            bb.contacts = c.m_nodeB.next;
        }

        if (c.m_manifold.pointCount > 0 && !a.isSensor() && !b.isSensor()) {
            aa.setAwake(true);
            bb.setAwake(true);
        }

        
        contactStacks[a.type().ordinal()][b.type().ordinal()].creator.push(c);

        --m_contactCount;
    }

    /**
     * This is the top level collision call for the time step. Here all the narrow phase collision is
     * processed for the world contact list.
     */
    public void collide() {
        
        Contact c = m_contactList;
        while (c != null) {
            Fixture fixtureA = c.aFixture;
            Fixture fixtureB = c.bFixture;
            int indexA = c.aIndex;
            int indexB = c.bIndex;
            Body2D bodyA = fixtureA.getBody();
            Body2D bodyB = fixtureB.getBody();

            
            if ((c.m_flags & Contact.FILTER_FLAG) == Contact.FILTER_FLAG) {
                
                if (!bodyB.colllide(bodyA)) {
                    Contact cNuke = c;
                    c = cNuke.next();
                    destroy(cNuke);
                    continue;
                }

                
                if (!ContactFilter.shouldCollide(fixtureA, fixtureB)) {
                    Contact cNuke = c;
                    c = cNuke.next();
                    destroy(cNuke);
                    continue;
                }

                
                c.m_flags &= ~Contact.FILTER_FLAG;
            }



            
            if (!(bodyA.isAwake() && bodyA.type != BodyType.STATIC) &&
                !(bodyB.isAwake() && bodyB.type != BodyType.STATIC)) {
                c = c.next();
                continue;
            }

            int proxyIdA = fixtureA.proxies[indexA].id;
            int proxyIdB = fixtureB.proxies[indexB].id;
            boolean overlap = broadPhase.testOverlap(proxyIdA, proxyIdB);

            
            if (!overlap) {
                Contact cNuke = c;
                c = cNuke.next();
                destroy(cNuke);
                continue;
            }

            
            c.update(contactListener);
            c = c.next();
        }
    }

    private Contact popContact(Fixture fixtureA, int indexA, Fixture fixtureB, int indexB) {

        if (fixtureA.getBody().getType()!= BodyType.DYNAMIC && fixtureB.getBody().getType()!= BodyType.DYNAMIC)
            return null;

        ShapeType type1 = fixtureA.type();
        ShapeType type2 = fixtureB.type();

        ContactRegister reg = contactStacks[type1.ordinal()][type2.ordinal()];
        if (reg != null) {
            if (reg.primary) {
                Contact c = reg.creator.pop();
                c.init(fixtureA, indexA, fixtureB, indexB);
                return c;
            } else {
                Contact c = reg.creator.pop();
                c.init(fixtureB, indexB, fixtureA, indexA);
                return c;
            }
        } else {
            return null;
        }
    }

}

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

package com.bulletphysics.collision.shapes.mesh;

import com.bulletphysics.collision.shapes.util.ScalarType;
import com.bulletphysics.collision.shapes.util.VertexData;
import com.bulletphysics.util.ObjectArrayList;

import javax.vecmath.Tuple3f;
import java.nio.ByteBuffer;

/**
 * TriangleIndexVertexArray allows to use multiple meshes, by indexing into existing
 * triangle/index arrays. Additional meshes can be added using {@link #addIndexedMesh addIndexedMesh}.<p>
 * 
 * No duplicate is made of the vertex/index data, it only indexes into external vertex/index
 * arrays. So keep those arrays around during the lifetime of this TriangleIndexVertexArray.
 * 
 * @author jezek2
 */
public class TriangleIndexVertexArray extends StridingMeshInterface {

	private final ObjectArrayList<IndexedMesh> indexedMeshes = new ObjectArrayList<>();

	private final ByteBufferVertexData data = new ByteBufferVertexData();

	public TriangleIndexVertexArray() {
	}

	/**
	 * Just to be backwards compatible.
	 */
	public TriangleIndexVertexArray(int numTriangles, ByteBuffer triangleIndexBase, int triangleIndexStride, int numVertices, ByteBuffer vertexBase, int vertexStride) {
		IndexedMesh mesh = new IndexedMesh();

		mesh.numTriangles = numTriangles;
		mesh.triangleIndexBase = triangleIndexBase;
		mesh.triangleIndexStride = triangleIndexStride;
		mesh.numVertices = numVertices;
		mesh.vertexBase = vertexBase;
		mesh.vertexStride = vertexStride;

		addIndexedMesh(mesh);
	}

	private void addIndexedMesh(IndexedMesh mesh) {
		addIndexedMesh(mesh, ScalarType.INTEGER);
	}

	private void addIndexedMesh(IndexedMesh mesh, ScalarType indexType) {
		indexedMeshes.add(mesh);
		indexedMeshes.get(indexedMeshes.size() - 1).indexType = indexType;
	}
	
	@Override
	public VertexData getLockedVertexIndexBase(int subpart) {
		assert (subpart < getNumSubParts());

		IndexedMesh mesh = indexedMeshes.get(subpart);

		data.vertexCount = mesh.numVertices;
		data.vertexData = mesh.vertexBase;
		//#ifdef BT_USE_DOUBLE_PRECISION
		//type = PHY_DOUBLE;
		//#else
		data.vertexType = ScalarType.FLOAT;
		//#endif
		data.vertexStride = mesh.vertexStride;

		data.indexCount = mesh.numTriangles*3;

		data.indexData = mesh.triangleIndexBase;
		data.indexStride = mesh.triangleIndexStride/3;
		data.indexType = mesh.indexType;
		return data;
	}

	@Override
	public VertexData getLockedReadOnlyVertexIndexBase(int subpart) {
		return getLockedVertexIndexBase(subpart);
	}

	/**
	 * unLockVertexBase finishes the access to a subpart of the triangle mesh.
	 * Make a call to unLockVertexBase when the read and write access (using getLockedVertexIndexBase) is finished.
	 */
	@Override
	public void unLockVertexBase(int subpart) {
		data.vertexData = null;
		data.indexData = null;
	}

	@Override
	public void unLockReadOnlyVertexBase(int subpart) {
		unLockVertexBase(subpart);
	}

	/**
	 * getNumSubParts returns the number of seperate subparts.
	 * Each subpart has a continuous array of vertices and indices.
	 */
	@Override
	public int getNumSubParts() {
		return indexedMeshes.size();
	}

	public ObjectArrayList<IndexedMesh> getIndexedMeshArray() {
		return indexedMeshes;
	}
	
	@Override
	public void preallocateVertices(int numverts) {
	}

	@Override
	public void preallocateIndices(int numindices) {
	}

	/**
	 *
	 * @author jezek2
	 */
	public static class ByteBufferVertexData extends VertexData {

		ByteBuffer vertexData;
		int vertexCount;
		int vertexStride;
		ScalarType vertexType;

		ByteBuffer indexData;
		int indexCount;
		int indexStride;
		ScalarType indexType;

		@Override
		public int getVertexCount() {
			return vertexCount;
		}

		@Override
		public int getIndexCount() {
			return indexCount;
		}

		@Override
		public <T extends Tuple3f> T getVertex(int idx, T out) {
			int off = idx*vertexStride;
			out.x = vertexData.getFloat(off+4*0);
			out.y = vertexData.getFloat(off+4*1);
			out.z = vertexData.getFloat(off+4*2);
			return out;
		}

		@Override
		public void setVertex(int idx, float x, float y, float z) {
			int off = idx*vertexStride;
			vertexData.putFloat(off+4*0, x);
			vertexData.putFloat(off+4*1, y);
			vertexData.putFloat(off+4*2, z);
		}

		@Override
		public int getIndex(int idx) {
			if (indexType == ScalarType.SHORT) {
				return indexData.getShort(idx*indexStride) & 0xFFFF;
			}
			else if (indexType == ScalarType.INTEGER) {
				return indexData.getInt(idx*indexStride);
			}
			else {
				throw new IllegalStateException("indicies type must be short or integer");
			}
		}

	}
}

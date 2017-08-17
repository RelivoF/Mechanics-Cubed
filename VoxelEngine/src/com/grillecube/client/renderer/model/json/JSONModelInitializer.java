package com.grillecube.client.renderer.model.json;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;

import com.grillecube.client.renderer.model.Model;
import com.grillecube.client.renderer.model.ModelInitializer;
import com.grillecube.client.renderer.model.ModelSkeleton;
import com.grillecube.client.renderer.model.ModelSkin;
import com.grillecube.client.renderer.model.animation.Bone;
import com.grillecube.client.renderer.model.animation.BoneTransform;
import com.grillecube.client.renderer.model.animation.KeyFrame;
import com.grillecube.client.renderer.model.animation.ModelSkeletonAnimation;
import com.grillecube.common.JSONHelper;
import com.grillecube.common.Logger;
import com.grillecube.common.maths.Matrix4f;
import com.grillecube.common.maths.Quaternion;
import com.grillecube.common.maths.Vector3f;

public class JSONModelInitializer implements ModelInitializer {

	protected final String dirpath;

	public JSONModelInitializer(String dirpath) {
		this.dirpath = dirpath;
	}

	/** initializer override */
	@Override
	public final void onInitialized(Model model) {

		try {
			// get the info file
			JSONObject json = new JSONObject(JSONHelper.readFile(this.dirpath + "info.json"));
			this.parseJSON(model, json);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	protected void parseJSON(Model model, JSONObject jsonInfo) throws JSONException, IOException {

		Logger.get().log(Logger.Level.FINE, "Parsing JSON Model", this.dirpath);

		// model name
		this.parseJSONName(model, jsonInfo);

		// get skeleton
		this.parseJSONSkeleton(model, jsonInfo);

		// get the mesh
		this.parseJSONMesh(model, jsonInfo);

		// get skins
		this.parseJSONSkins(model, jsonInfo);

		// get animations
		this.parseJSONAnimations(model, jsonInfo);
	}

	protected void parseJSONName(Model model, JSONObject jsonInfo) {
		String modelName = jsonInfo.has("name") ? jsonInfo.getString("name") : null;
		model.setName(modelName);
	}

	protected void parseJSONSkeleton(Model model, JSONObject jsonInfo) {
		String skeletonPath = this.dirpath + jsonInfo.getString("skeleton");
		try {

			ModelSkeleton modelSkeleton = model.getSkeleton();
			JSONObject skeleton = new JSONObject(JSONHelper.readFile(skeletonPath));
			// get the root bone
			modelSkeleton.setRootBone(skeleton.getString("rootBone"));

			// get all bones
			JSONArray jsonBones = skeleton.getJSONArray("bones");
			for (int i = 0; i < jsonBones.length(); i++) {
				JSONObject jsonBone = jsonBones.getJSONObject(i);
				String boneName = jsonBone.getString("name");

				Matrix4f boneBindTransform = new Matrix4f();
				if (jsonBone.has("bindTransform")) {
					JSONObject jsonBindTransform = jsonBone.getJSONObject("bindTransform");
					JSONObject jsonBindTransformRotation = jsonBindTransform.getJSONObject("rotation");
					float x = (float) jsonBindTransformRotation.getDouble("x");
					float y = (float) jsonBindTransformRotation.getDouble("y");
					float z = (float) jsonBindTransformRotation.getDouble("z");
					float w = (float) jsonBindTransformRotation.getDouble("w");
					Matrix4f.mul(boneBindTransform, Quaternion.toRotationMatrix(x, y, z, w), boneBindTransform);
				}

				String boneParentName = jsonBone.has("parentName") ? jsonBone.getString("parentName") : null;

				// add the bone
				Bone bone = modelSkeleton.addBone(boneName, boneParentName, boneBindTransform);

				// set children
				JSONArray childrenNames = jsonBone.has("childrenNames") ? jsonBone.getJSONArray("childrenNames") : null;

				if (childrenNames != null) {
					for (int j = 0; j < childrenNames.length(); j++) {
						bone.addChild(childrenNames.getString(j));
					}
				}
			}

			// finally, calculate bind matrices recursively
			modelSkeleton.getRootBone().calcInverseBindTransform(Matrix4f.IDENTITY);

		} catch (Exception e) {
			e.printStackTrace(Logger.get().getPrintStream());
		}
	}

	private void parseJSONMesh(Model model, JSONObject jsonInfo) {
		String meshpath = this.dirpath + jsonInfo.getString("mesh");
		try {
			JSONObject mesh = new JSONObject(JSONHelper.readFile(meshpath));
			JSONArray vertices = mesh.getJSONArray("vertices");
			ByteBuffer verticesBuffer = BufferUtils.createByteBuffer(vertices.length() * 4);
			int i = 0;
			while (i < vertices.length()) {

				float x = (float) vertices.getDouble(i++);
				float y = (float) vertices.getDouble(i++);
				float z = (float) vertices.getDouble(i++);

				float uvx = (float) vertices.getDouble(i++);
				float uvy = (float) vertices.getDouble(i++);

				float nx = (float) vertices.getDouble(i++);
				float ny = (float) vertices.getDouble(i++);
				float nz = (float) vertices.getDouble(i++);

				int j1 = this.getBoneID(model, vertices.getString(i++));
				int j2 = this.getBoneID(model, vertices.getString(i++));
				int j3 = this.getBoneID(model, vertices.getString(i++));

				float w1 = (float) vertices.getDouble(i++);
				float w2 = (float) vertices.getDouble(i++);
				float w3 = (float) vertices.getDouble(i++);

				verticesBuffer.putFloat(x);
				verticesBuffer.putFloat(y);
				verticesBuffer.putFloat(z);

				verticesBuffer.putFloat(uvx);
				verticesBuffer.putFloat(uvy);

				verticesBuffer.putFloat(nx);
				verticesBuffer.putFloat(ny);
				verticesBuffer.putFloat(nz);

				verticesBuffer.putInt(j1);
				verticesBuffer.putInt(j2);
				verticesBuffer.putInt(j3);

				verticesBuffer.putFloat(w1);
				verticesBuffer.putFloat(w2);
				verticesBuffer.putFloat(w3);
			}

			verticesBuffer.flip();
			model.getMesh().setVertices(verticesBuffer);

			JSONArray indices = mesh.getJSONArray("indices");
			// short buffer
			ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length() * 2);
			i = 0;
			while (i < indices.length()) {
				indicesBuffer.putShort((short) indices.getInt(i++));
			}
			indicesBuffer.flip();
			model.getMesh().setIndices(indicesBuffer);

		} catch (Exception e) {
			e.printStackTrace(Logger.get().getPrintStream());
		}
	}

	protected int getBoneID(Model model, String boneName) {
		Bone bone = model.getSkeleton().getBone(boneName);
		return (bone == null ? 0 : bone.getID());
	}

	protected void parseJSONSkins(Model model, JSONObject jsonInfo) {
		JSONArray skins = jsonInfo.getJSONArray("skins");
		for (int i = 0; i < skins.length(); i++) {
			String skinpath = this.dirpath + skins.getString(i);
			try {
				JSONObject skin = new JSONObject(JSONHelper.readFile(skinpath));
				String name = skin.getString("name");
				String texture = this.dirpath + skin.getString("texture");
				model.addSkin(new ModelSkin(name, texture));
			} catch (Exception e) {
				e.printStackTrace(Logger.get().getPrintStream());
			}
		}
	}

	protected void parseJSONAnimations(Model model, JSONObject jsonInfo) {

		JSONArray animations = jsonInfo.getJSONArray("animations");
		for (int i = 0; i < animations.length(); i++) {
			String animpath = this.dirpath + animations.getString(i);
			JSONObject animation;
			try {
				animation = new JSONObject(JSONHelper.readFile(animpath));
			} catch (Exception e) {
				e.printStackTrace(Logger.get().getPrintStream());
				continue;
			}
			String name = animation.getString("name");
			long duration = animation.getLong("duration");
			JSONArray jsonKeyFrames = animation.getJSONArray("keyFrames");
			ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>(jsonKeyFrames.length());

			for (int j = 0; j < jsonKeyFrames.length(); j++) {
				JSONObject jsonKeyFrame = jsonKeyFrames.getJSONObject(j);
				long time = jsonKeyFrame.getLong("time");
				HashMap<String, BoneTransform> boneTransforms = new HashMap<String, BoneTransform>();
				JSONArray pose = jsonKeyFrame.getJSONArray("pose");
				for (int k = 0; k < pose.length(); k++) {
					JSONObject bonePose = pose.getJSONObject(k);
					String boneName = bonePose.getString("bone");
					JSONObject jsonTransform = bonePose.getJSONObject("transform");
					JSONObject jsonPosition = jsonTransform.getJSONObject("position");

					float x, y, z, w;

					x = (float) jsonPosition.getDouble("x");
					y = (float) jsonPosition.getDouble("y");
					z = (float) jsonPosition.getDouble("z");

					Vector3f position = new Vector3f(x, y, z);

					JSONObject jsonQuaternion = jsonTransform.getJSONObject("rotation");
					x = (float) jsonQuaternion.getDouble("x");
					y = (float) jsonQuaternion.getDouble("y");
					z = (float) jsonQuaternion.getDouble("z");
					w = (float) jsonQuaternion.getDouble("w");

					Quaternion rotation = new Quaternion(x, y, z, w);

					BoneTransform boneTransform = new BoneTransform(position, rotation);
					boneTransforms.put(boneName, boneTransform);
				}

				KeyFrame keyFrame = new KeyFrame(time, boneTransforms);
				keyFrames.add(keyFrame);
			}

			ModelSkeletonAnimation modelAnimation = new ModelSkeletonAnimation(name, duration, keyFrames);
			model.addAnimation(modelAnimation);
		}
	}

	@Override
	public String toString() {
		return (this.getClass().getSimpleName() + " : " + this.dirpath);
	}

}
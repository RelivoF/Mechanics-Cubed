package com.grillecube.client.renderer.model.editor;

import com.grillecube.client.VoxelEngineClient;
import com.grillecube.client.renderer.camera.CameraProjectiveWorld;
import com.grillecube.client.renderer.model.editor.gui.GuiModelEditor;

public class ModelEditor {

	public static ModelEditor instance;

	public static void main(String[] args) {
		new ModelEditor().run();
	}

	private void run() {

		instance = this;

		/* 1 */
		// initialize engine
		VoxelEngineClient engine = new VoxelEngineClient();

		/* 2 */
		// inject resources to be loaded
		engine.getModLoader().injectMod(ModelEditorMod.class);

		// load resources (mods)
		engine.load();

		/* prepare engine before looping */
		this.prepareEngine(engine);

		/* 3 */
		// loop, every allocated memory will be released properly on program
		// termination */
		try {
			engine.loop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		engine.stopAll();
	}

	private void prepareEngine(VoxelEngineClient engine) {

		engine.setWorld(ModelEditorMod.WORLD_ID);

		engine.getGLFWWindow().swapInterval(1);
		engine.getGLFWWindow().setScreenPosition(100, 100);

		CameraProjectiveWorld camera = new ModelEditorCamera(engine.getGLFWWindow());
		engine.getRenderer().setCamera(camera);

		GuiModelEditor guiModelEditor = new GuiModelEditor();
		engine.getRenderer().getGuiRenderer().addGui(guiModelEditor);
	}
}
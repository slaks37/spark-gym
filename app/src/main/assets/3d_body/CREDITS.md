# Third-party assets in this folder

## muscles.glb
Muscle geometry derived from **Z-Anatomy**, licensed **CC BY-SA 4.0**.

- Source: https://www.z-anatomy.com — https://github.com/LluisV/Z-Anatomy
- Original file: `Resources/Models/FBX/MuscularSystem100.fbx`

Changes made: the 411 individually-named muscles were filtered to the
superficial ones the heat map reasons about, merged into the 19 groups the app
tracks, decimated by vertex clustering to roughly 240k triangles so it runs in a
WebView on a phone, and exported to binary glTF.

CC BY-SA 4.0 is share-alike, so this derived model stays under CC BY-SA 4.0 and
this notice must travel with it. The rest of Spark Gym remains MIT.

## three.min.js, OrbitControls.js, GLTFLoader.js
Three.js r128 — MIT. https://threejs.org

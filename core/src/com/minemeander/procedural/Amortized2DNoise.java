package com.minemeander.procedural;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;

import com.minemeander.engine.tiles.WorldTile;

public class Amortized2DNoise {
	float[] uax, vax, ubx, vbx, uay, vay, uby, vby; // /< Amortized noise tables.
	float[] spline; // /< Spline array.
	float[][] workspace; // /< Temporary workspace.
	int size; // /< Size of workspace.

	public Amortized2DNoise(int n) {
		uax = new float[n + 1];
		vax = new float[n + 1];
		ubx = new float[n + 1];
		vbx = new float[n + 1];
		uay = new float[n + 1];
		uby = new float[n + 1];
		vay = new float[n + 1];
		vby = new float[n + 1];
		spline = new float[n + 1];
		workspace = new float[n + 1][n + 1];
		this.size = n;
	}

	public void fillUp(float[] t, float s, int n) {
		float d = s / n;
		t[0] = 0.0f;
		t[1] = d;
		for (int i = 2; i <= n; i++)
			t[i] = t[i - 1] + d;
	}

	public void fillDn(float[] t, float s, int n) {
		float d = -s / n;
		t[n] = 0.0f;
		t[n - 1] = d;
		for (int i = n - 2; i >= 0; i--)
			t[i] = t[i + 1] + d;

	} // FillDn

	public void initspline(int n) {
		for (int i = 0; i <= n; i++) {
			float t = (float) i / n;
			spline[i] = t * t * t * (10.0f + 3.0f * t * (2.0f * t - 5.0f));
		}
	}

	public int h1(int x) {
		return x * x;
	} // h1

	public float h2(int x, int y) {
		return (float) h1(h1(x) + y);
	} // h2

	public void initAmortizedNoise(int x0, int y0, int n) {
		initspline(n);

		// compute gradients at corner points
		float b0 = h2(x0, y0), b1 = h2(x0, y0 + 1), b2 = h2(x0 + 1, y0), b3 = h2(x0 + 1, y0 + 1);

		// fill inferred gradient tables from corner gradients
		fillUp(uax, (float) Math.cos(b0), n);
		fillDn(vax, (float) Math.cos(b1), n);
		fillUp(ubx, (float) Math.cos(b2), n);
		fillDn(vbx, (float) Math.cos(b3), n);
		fillUp(uay, (float) Math.sin(b0), n);
		fillUp(vay, (float) Math.sin(b1), n);
		fillDn(uby, (float) Math.sin(b2), n);
		fillDn(vby, (float) Math.sin(b3), n);

	} // initAmortizedNoise

	public void getAmortizedNoise(int n) {
		float u, v, a, b;
		for (int i = 0; i <= n; i++)
			for (int j = 0; j <= n; j++) {
				u = uax[j] + uay[i];
				v = vax[j] + vay[i];
				a = lerp(spline[j], u, v);
				u = ubx[j] + uby[i];
				v = vbx[j] + vby[i];
				b = lerp(spline[j], u, v);
				workspace[i][j] = lerp(spline[i], a, b);
			} // for
	} // getAmortizedNoise

	public float lerp(float t, float a, float b) {
		return (a + t * (b - a));
	}

	public float getAmortizedNoise(int x, int y, int m0, int m1, int n, float[][] cell) {
		int s = n, r = 1;

		// skip over unwanted octaves
		for (int i = 1; i < m0; i++) {
			s /= 2;
			r *= 2;
		} // for

		float scale = 1.0f;

		// generate each octave into the workspace and add into the cell
		for (int k = m0; k <= m1 && s >= 2; k++) { // for each octave
			for (int i0 = 0; i0 < r; i0++)
				for (int j0 = 0; j0 < r; j0++) {
					initAmortizedNoise(x + i0, y + j0, s);
					getAmortizedNoise(s);
					for (int i1 = 0; i1 < s; i1++)
						for (int j1 = 0; j1 < s; j1++)
							cell[i0 * s + i1][j0 * s + j1] += workspace[i1][j1] * scale;
				} // for
			s = s / 2;
			r += r;
			scale /= 2.0f;
		} // for each octave

		// return cell;
		return 0.7071f / (1.0f - scale); // scale factor
	} // getAmortize

	public float generate2DNoise(TiledMapTileLayer layer, TiledMapTileSet tileSet, WorldTile tile, WorldTile tile2, int octave0, int octave1, int incX, int incY) {

		float[][] cell = new float[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cell[i][j] = 0f;
			}
		}
		
		float scale = getAmortizedNoise(incX, incY, octave0, octave1, size, cell);

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				if (cell[x][y] > 0) {									
					layer.setCell(x, y, Math.random() > 0.5f ? tile.toCell(tileSet) : tile2.toCell(tileSet));
				}
			}
		}

		return scale;
	} // Gene

}

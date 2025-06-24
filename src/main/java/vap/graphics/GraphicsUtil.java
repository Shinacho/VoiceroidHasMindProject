/*
 * The MIT License
 *
 * Copyright 2025 owner.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package vap.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @version 1.0.0 - 2021/08/17_12:01:37<br>
 * @author Shinacho<br>
 */
public class GraphicsUtil {

	public static Color createColor(List<String> rgba) throws ColorException {
		if (rgba.isEmpty() || rgba.size() <= 2 || rgba.size() >= 5) {
			throw new ColorException(rgba);
		}
		int r = Integer.parseInt(rgba.get(0));
		int g = Integer.parseInt(rgba.get(1));
		int b = Integer.parseInt(rgba.get(2));
		int a = rgba.size() <= 3 ? 255 : Integer.parseInt(rgba.get(3));
		return new Color(r, g, b, a);
	}

	public static Color createColor(String[] rgba) throws ColorException {
		return createColor(Arrays.asList(rgba));

	}

	public static Color reverse(Color c) {
		int r = 255 - c.getRed();
		int g = 255 - c.getGreen();
		int b = 255 - c.getBlue();
		int a = 255 - c.getAlpha();
		return new Color(r, g, b, a);
	}

	/**
	 * インスタンス化できません.
	 */
	private GraphicsUtil() {
	}

	/**
	 * Java2DのOpenGLパイプラインを有効化します. 環境によっては、描画パフォーマンスが向上する場合があります。<br>
	 */
	public static void useOpenGL() {
		System.setProperty("sun.java2d.opengl", "true");
	}

	/**
	 * OpenGLパイプラインを使用しているかを検査します.
	 *
	 * @return OpenGLパイプラインを使用している場合は、trueを返します。<br>
	 */
	public static boolean isUseOpenGL() {
		return System.getProperty("sun.java2d.opengl").equals("true");
	}

	/**
	 * Rectangle2Dインスタンスを使用して、drawRectを実行します.
	 *
	 * @param g 書き込むグラフィックスコンテキストを指定します。<br>
	 * @param r 描画範囲となるRectangle2Dインスタンスを指定します。<br>
	 */
	public static void drawRect(Graphics2D g, Rectangle2D r) {
		g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
	}

	public static Color transparent(Color c, int a) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
	}
}

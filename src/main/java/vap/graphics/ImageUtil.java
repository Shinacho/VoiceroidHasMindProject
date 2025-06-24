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

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * 画像のIOや簡易編集を行うユーティリティクラスです.
 * <br>
 * このクラスからロードした画像は、通常の方法でロードされた画像よりも 高速に描画できる可能性があります。
 * また、このクラスのロード機能は、同じファイルパスを指定すると 同じ画像インスタンスを返します。<br>
 * <br>
 *
 * @version 1.0.0 - 2013/01/13_2:08:33<br>
 * @version 1.1.0 - 2013/04/28_23:16<br>
 * @version 1.2.0 - 2023/08/29_22:51<br>
 * @author Shinacho<br>
 */
public final class ImageUtil {

	/**
	 * デフォルトのウインドウシステムがサポートする画像の生成機能を持った、グラフィックスの設定です.
	 */
	private static final GraphicsConfiguration gc
			= GraphicsEnvironment.getLocalGraphicsEnvironment().
					getDefaultScreenDevice().getDefaultConfiguration();
	/**
	 * ロードした画像をキャッシュするためのマップです.
	 */
	private static final HashMap<String, WeakReference<BufferedImage>> IMAGE_CACHE
			= new HashMap<>(32);

	/**
	 * メインスクリーンのデバイス設定を取得します。<br>
	 *
	 * @return デバイスの設定。このインスタンスから画像を作成できます。<br>
	 */
	public static GraphicsConfiguration getGraphicsConfiguration() {
		return gc;
	}

	/**
	 * ユーティリティクラスのためインスタンス化できません.
	 */
	private ImageUtil() {
	}

	//------------------------------------------------------------------------------------------------------------
	/**
	 * 新しい空のBufferedImageを生成します. 作成された画像は全てのピクセルが完全に透明な黒(0x00000000)です。<br>
	 *
	 * @param width 画像の幅をピクセル単位で指定します。<br>
	 * @param height 画像の高さをピクセル単位で指定します。<br>
	 *
	 * @return BufferedImageの新しいインスタンスを返します。<br>
	 */
	public static BufferedImage newImage(int width, int height) {
		return gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}

	/**
	 * BufferedImageの複製を新しいインスタンスとして返します.
	 *
	 * @param src コピーする画像。<br>
	 *
	 * @return srcと同じ画像の新しいインスタンスを返します。<br>
	 */
	public static BufferedImage copy(BufferedImage src) {
		return copy(src, (BufferedImage) null);
	}

	/**
	 * BufferedImageの複製を作成し、dstに格納します.
	 *
	 * @param src コピーする画像。<br>
	 * @param dst nullでない場合このインスタンスに結果が格納される。<br>
	 *
	 * @return nullでない場合、この引数に結果が格納されます。<br>
	 */
	public static BufferedImage copy(BufferedImage src, BufferedImage dst) {
		if (dst == null || dst == src) {
			dst = newImage(src.getWidth(), src.getHeight());
		}
		Graphics2D g2 = dst.createGraphics();
		g2.setRenderingHints(RenderingQuality.QUALITY.getRenderingHints());
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		return dst;
	}

	/**
	 * BufferedImageをファイルから作成します.
	 * このメソッドはすでに一度要求された画像を再度要求した場合、同じインスタンスを返します。<br>
	 * 確実に別のインスタンスを取得する場合はこのメソッドの戻り値に対してこのクラスのcopyメソッドを使用してください。<br>
	 *
	 * @param filePath 読み込むファイルパス。<br>
	 *
	 * @return 読み込まれた画像.すでに一度読み込まれている場合はキャッシュデータの同じ画像インスタンスを返す。<br>
	 *
	 * @throws ContentsFileNotFoundException ファイルが存在しない場合に投げられる。<br>
	 * @throws ContentsIOException ファイルがロードできない場合に投げられます。<br>
	 */
	public static BufferedImage load(String filePath) throws FileNotFoundException, ContentsIOException {
		WeakReference<BufferedImage> cacheRef = IMAGE_CACHE.get(filePath);
		//キャッシュあり&GC未実行
		if (cacheRef != null) {
			var v = cacheRef.get();
			if (v != null) {
				return v;
			}
		}
		//GCが実行されているかキャッシュがなければ新しくロードしてキャッシュに追加する
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("notfound : filePath=[" + filePath + "]");
		}
		BufferedImage dst = null;
		try {
			dst = ImageIO.read(file);
		} catch (IOException ex) {
			throw new ContentsIOException(ex);
		}
		if (dst == null) {
			throw new ContentsIOException("image is null");
		}
		//互換画像に置換
		BufferedImage newImage = newImage(dst.getWidth(), dst.getHeight());
		Graphics2D g2 = createGraphics2D(newImage, RenderingQuality.QUALITY);
		g2.drawImage(dst, 0, 0, null);
		g2.dispose();
		IMAGE_CACHE.put(filePath, new WeakReference<>(newImage));
		return newImage;
	}

	/**
	 * BufferedImageをファイルに保存します. 画像形式は透過PNG画像となります。<br>
	 *
	 * @param filePath 書き込むファイルパス.上書きは確認されず、拡張子も任意。<br>
	 * @param image 書き込む画像。<br>
	 *
	 * @throws ContentsIOException ファイルが書き込めない場合に投げられる。<br>
	 */
	public static void save(String filePath, BufferedImage image) throws ContentsIOException {
		save(new File(filePath), image);
	}

	public static void save(File f, BufferedImage image) throws ContentsIOException {
		try {
			ImageIO.write(image, "PNG", f);
		} catch (IOException ex) {
			throw new ContentsIOException(ex);
		}
	}

	/**
	 * BufferedImageのピクセルデータを配列として取得します.
	 *
	 * @param image ピクセルデータを取得する画像を送信します。<br>
	 *
	 * @return 指定された画像のピクセルデータを一次元配列として返します。 この配列は画像に設定されているピクセルのクローンです。<br>
	 */
	public static int[] getPixel(BufferedImage image) {
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}

	/**
	 * BufferedImageのピクセルデータを二次元配列として取得します.
	 *
	 * @param image ピクセルデータを取得する画像を送信します。<br>
	 *
	 * @return 指定された画像のピクセルデータを二次元配列として返します。 この配列は画像に設定されているピクセルのクローンです。<br>
	 */
	public static int[][] getPixel2D(BufferedImage image) {
		int[] pix = getPixel(image);
		int[][] pix2 = new int[image.getHeight()][image.getWidth()];
		for (int i = 0, row = 0, WIDTH = image.getWidth(); i < pix2.length; i++, row += WIDTH) {
			System.arraycopy(pix, row, pix2[i], 0, WIDTH);
		}
		return pix2;
	}

	/**
	 * BufferedImageにピクセルデータを設定します.
	 * このメソッドはピクセル数と画像の実際のピクセル数が異なる場合の動作は定義されていません。<br>
	 *
	 * @param image ピクセルデータを設定する画像。<br>
	 * @param pix 設定するピクセルデータ。<br>
	 */
	public static void setPixel(BufferedImage image, int[] pix) {
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), pix, 0, image.getWidth());
	}

	/**
	 * BufferedImageにピクセルデータを設定します.
	 * このメソッドはピクセル数と画像の実際のピクセル数が異なる場合の動作は定義されていません。<br>
	 *
	 * @param image 画像。<br>
	 * @param pix 設定するピクセルデータ。<br>
	 */
	public static void setPixel2D(BufferedImage image, int[][] pix) {
		int[] newPix = new int[getPixel(image).length];
		for (int i = 0; i < pix.length; i++) {
			System.arraycopy(pix[i], 0, newPix, i * pix[0].length, pix[i].length);
		}
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), newPix, 0, image.getWidth());
	}

	public static int getPixel(BufferedImage image, int x, int y) {
		return getPixel2D(image)[y][x];
	}

	/**
	 * 画像に書き込むためのグラフィクスコンテキストを作成します.
	 *
	 * @param image グラフィックスコンテキストを取得する画像を指定します。 <br>
	 * @param renderingPolicy nullでない場合、このレンダリング設定がグラフィックスコンテキストに適用されます。<br>
	 *
	 * @return 指定した画像に書き込むためのグラフィックスコンテキストを作成して返します。<br>
	 */
	public static Graphics2D createGraphics2D(BufferedImage image, RenderingQuality renderingPolicy) {
		Graphics2D g = image.createGraphics();
		if (renderingPolicy != null) {
			g.setRenderingHints(renderingPolicy.getRenderingHints());
		}
		g.setClip(0, 0, image.getWidth(), image.getHeight());
		return g;
	}
}

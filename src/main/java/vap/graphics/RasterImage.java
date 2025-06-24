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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import static vap.graphics.ImageUtil.setPixel2D;
import static vap.graphics.ImageUtil.getPixel2D;
import vap.graphics.RasterImage.Raster.Pixel;

/**
 *
 * @version 1.0.0 - 2021/08/17_6:22:24<br>
 * @author Shinacho<br>
 */
public class RasterImage implements Cloneable {

	public final class Raster implements Iterable<RasterImage.Raster.Pixel> {

		private Pixel[][] data;

		private Raster() {
		}

		private void setData(Pixel[][] data) {
			this.data = data;
		}

		public Pixel of(int x, int y) throws GraphicsException {
			return data[y][x];
		}

		@Deprecated
		public Pixel[][] all() {
			return data;
		}

		@Deprecated
		public int[][] asIntArray() {
			return getPixel2D();
		}

		public Raster set(UnaryOperator<Pixel> u) {
			Pixel[][] res = new Pixel[getHeight()][];
			Raster r = new Raster();
			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					res[y][x] = u.apply(this.data[y][x]);
				}
			}
			r.setData(res);
			return r;
		}

		public RasterImage updateImage() {
			return RasterImage.this.updateImage(this);
		}

		public RasterImage newImage() {
			return RasterImage.this.clone().updateImage(this);
		}

		public Raster rotate(float angle) {
			while (angle >= 360.0f) {
				angle -= 360.0f;
			}
			while (angle < 0.0f) {
				angle += 360.0f;
			}
			if (angle == 0f) {
				return this;
			}

			if (angle % 90 == 0) {
				for (int i = 0; i < (int) (angle / 90); i++) {
					data = rotate(data);
				}
				return this;
			} else {
				this.data = RasterImage.this.rotate(angle).asRaster().data;
				return this;
			}

		}

		private static Pixel[][] rotate(Pixel[][] p) {
			int n = p.length;
			int m = p[0].length;
			Pixel[][] res = new Pixel[m][n];

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					res[j][n - 1 - i] = p[i][j];
				}
			}
			return res;
		}

		public Stream<Pixel> stream() {
			return Arrays.stream(data).flatMap(Arrays::stream);
		}

		@Override
		public Iterator<Pixel> iterator() {
			return Arrays.stream(data).flatMap(Arrays::stream).toList().iterator();
		}

		private int[][] getPixel2D() {
			int[][] res = new int[data.length][];
			for (int y = 0; y < data.length; y++) {
				res[y] = new int[data[y].length];
				for (int x = 0; x < data[y].length; x++) {
					res[y][x] = data[y][x].value;
				}
			}
			return res;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 29 * hash + Arrays.deepHashCode(this.data);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Raster other = (Raster) obj;
			return Arrays.deepEquals(this.data, other.data);
		}

		@Override
		public String toString() {
			return "Raster{" + "data=" + getSize() + '}';
		}

		public final class Pixel {

			private int value;

			private Pixel(int value) {
				this.value = value;
			}

			public Color asAWTColor() {
				return ARGBColor.toAWTColor(value);
			}

			public int asARGB() {
				return value;
			}

			public Pixel to透明() {
				this.value = ARGBColor.toARGB(getR(), getG(), getB(), ARGBColor.ALPHA_TRANSPARENT);
				return this;
			}

			public Pixel to不透明() {
				this.value = ARGBColor.toARGB(getR(), getG(), getB(), ARGBColor.ALPHA_OPAQUE);
				return this;
			}

			public int getA() {
				return ARGBColor.getAlpha(value);
			}

			public int getR() {
				return ARGBColor.getRed(value);
			}

			public int getG() {
				return ARGBColor.getGreen(value);
			}

			public int getB() {
				return ARGBColor.getBlue(value);
			}

			public Pixel addA(int v) {
				int nv = getA() + v;
				return setA(nv);
			}

			public Pixel mulA(float v) {
				int nv = (int) (getA() * v);
				return setA(nv);
			}

			public Pixel addR(int v) {
				int nv = getR() + v;
				return setR(nv);
			}

			public Pixel mulR(float v) {
				int nv = (int) (getR() * v);
				return setR(nv);
			}

			public Pixel addG(int v) {
				int nv = getG() + v;
				return setG(nv);
			}

			public Pixel mulG(float v) {
				int nv = (int) (getG() * v);
				return setG(nv);
			}

			public Pixel addB(int v) {
				int nv = getB() + v;
				return setB(nv);
			}

			public Pixel mulB(float v) {
				int nv = (int) (getB() * v);
				return setB(nv);
			}

			public Pixel setA(int v) {
				if (v > 255) {
					v = 255;
				}
				if (v < 0) {
					v = 0;
				}
				this.value = ARGBColor.toARGB(getR(), getG(), getB(), v);
				return this;
			}

			public Pixel setR(int v) {
				if (v > 255) {
					v = 255;
				}
				if (v < 0) {
					v = 0;
				}
				this.value = ARGBColor.toARGB(v, getG(), getB(), getA());
				return this;
			}

			public Pixel setG(int v) {
				if (v > 255) {
					v = 255;
				}
				if (v < 0) {
					v = 0;
				}
				this.value = ARGBColor.toARGB(getR(), v, getB(), getA());
				return this;
			}

			public Pixel setB(int v) {
				if (v > 255) {
					v = 255;
				}
				if (v < 0) {
					v = 0;
				}
				this.value = ARGBColor.toARGB(getR(), getG(), v, getA());
				return this;
			}

			public Pixel to(int argb) {
				this.value = argb;
				return this;
			}

			public Pixel to(int r, int g, int b, int a) {
				this.value = ARGBColor.toARGB(a, r, g, b);
				return this;
			}

			public Pixel to(Color c) {
				this.value = ARGBColor.toARGB(c);
				return this;
			}

			public Pixel to(Pixel c) {
				this.value = c.value;
				return this;
			}

			public Pixel reverse() {
				return new Pixel(ARGBColor.reverse(value));
			}

			public Pixel average() {
				int a = getR() + getG() + getB() / 3;
				return to(a, a, a, getA());
			}

			public boolean is完全透明() {
				return getA() == ARGBColor.ALPHA_TRANSPARENT;
			}

			public boolean is透明() {
				return getA() != ARGBColor.ALPHA_OPAQUE;
			}

			public boolean is不透明() {
				return getA() == ARGBColor.ALPHA_OPAQUE;
			}

			public RasterImage updateImage() {
				return Raster.this.updateImage();
			}

			@Override
			public String toString() {
				return "Pixel{" + "value=" + Integer.toHexString(value) + '}';
			}

			@Override
			public int hashCode() {
				int hash = 7;
				hash = 37 * hash + this.value;
				return hash;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				final Pixel other = (Pixel) obj;
				return this.value == other.value;
			}

		}

	}

	public RasterImage updateImage(Raster r) {
		ImageUtil.setPixel2D(image, r.getPixel2D());
		return this;
	}

	protected BufferedImage image;

	public RasterImage(BufferedImage image) {
		this.image = image;
	}

	public RasterImage(int w, int h) {
		this(ImageUtil.newImage(w, h));
	}

	public RasterImage(File f) {
		this(f.getAbsolutePath());
	}

	public RasterImage(String path) {
		this(ImageUtil.load(path));
	}

	public RasterImage(RasterImage i) {
		this(i.image);
	}

	public Pixel of(int x, int y) throws IndexOutOfBoundsException {
		return asRaster().of(x, y);
	}

	public Raster asRaster() {
		int[][] pix = ImageUtil.getPixel2D(image);
		Pixel[][] data = new Pixel[getHeight()][];
		Raster r = new Raster();
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				data[y][x] = r.new Pixel(pix[y][x]);
			}
		}
		r.setData(data);
		return r;
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public java.awt.Image asAWTImage() {
		return image;
	}

	public ImageIcon asImageIcon() {
		return new ImageIcon(image);
	}

	public BufferedImage asBufferedImage() {
		return image;
	}

	@Override
	public RasterImage clone() {
		try {
			var r = (RasterImage) super.clone();
			r.image = ImageUtil.copy(image);
			return r;
		} catch (CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}

	@Override
	public String toString() {
		return "RasterImage{" + "image=" + (image != null) + ", size=" + getSize() + '}';
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 47 * hash + Objects.hashCode(this.image);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RasterImage other = (RasterImage) obj;
		return Objects.equals(this.image, other.image);
	}

	public void draw(Graphics2D g, int x, int y) {

	}

	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	public boolean sizeIs(RasterImage i) {
		return sizeIs(i.getSize());
	}

	public boolean sizeIs(Dimension2D i) {
		int w = (int) i.getWidth();
		int h = (int) i.getHeight();
		return this.getWidth() == w && this.getHeight() == h;
	}

	public static void saveTo(String filePath, BufferedImage image) throws ContentsIOException {
		saveTo(new File(filePath), image);
	}

	public static void saveTo(File f, BufferedImage image) throws ContentsIOException {
		try {
			ImageIO.write(image, "PNG", f);
		} catch (IOException ex) {
			throw new ContentsIOException(ex);
		}
	}

	public static void saveTo(String filePath, RasterImage i) throws ContentsIOException {
		saveTo(new File(filePath), i.image);
	}

	public static void saveTo(File f, RasterImage i) throws ContentsIOException {
		try {
			ImageIO.write(i.image, "PNG", f);
		} catch (IOException ex) {
			throw new ContentsIOException(ex);
		}
	}

	public void saveTo(File f) throws ContentsIOException {
		saveTo(f, this);
	}

	public void saveTo(String path) throws ContentsIOException {
		saveTo(path, this);
	}

	public Graphics2D createGraphics2D() {
		return createGraphics2D(RenderingQuality.DEFAULT);
	}

	public Graphics2D createGraphics2D(RenderingQuality renderingPolicy) {
		Graphics2D g = image.createGraphics();
		if (renderingPolicy != null) {
			g.setRenderingHints(renderingPolicy.getRenderingHints());
		}
		g.setClip(0, 0, image.getWidth(), image.getHeight());
		return g;
	}

	public RasterImage fillBy(Color c) {
		return asRaster().set(p -> p.to(c)).newImage();
	}

	public RasterImage fillBy(int c) {
		return asRaster().set(p -> p.to(c)).newImage();
	}

	public RasterImage fillBy(int r, int g, int b, int a) {
		return asRaster().set(p -> p.to(r, g, b, a)).newImage();
	}

	public List<RasterImage> splitRows(int y, int w, int h) throws GraphicsException {
		try {
			BufferedImage[] dst = new BufferedImage[image.getWidth() / w];
			for (int i = 0, x = 0; i < dst.length; i++, x += w) {
				dst[i] = image.getSubimage(x, y, w, h);
			}
			return Arrays.asList(dst).stream().map(p -> new RasterImage(image)).toList();
		} catch (RasterFormatException e) {
			throw new GraphicsException(e);
		}
	}

	public List<RasterImage> splitColumns(int x, int w, int h) throws GraphicsException {
		try {
			BufferedImage[] dst = new BufferedImage[image.getHeight() / h];
			for (int i = 0, y = 0; i < dst.length; i++, y += h) {
				dst[i] = image.getSubimage(x, y, w, h);
			}
			return Arrays.asList(dst).stream().map(p -> new RasterImage(image)).toList();
		} catch (RasterFormatException e) {
			throw new GraphicsException(e);
		}
	}

	public List<RasterImage> splitX(int w, int h) throws GraphicsException {
		return splitColumns(0, w, h);
	}

	public List<List<RasterImage>> split2D(int w, int h) throws GraphicsException {
		List<List<RasterImage>> result = new ArrayList<>();
		for (int i = 0, y = 0; i < image.getHeight() / h; i++, y += h) {
			result.add(new ArrayList<>(splitRows(y, w, h)));
		}
		return result;
	}

	public Map<String, RasterImage> splitAsMap(int w, int h, BiFunction<Integer, Integer, String> nameMapper)
			throws GraphicsException {
		Map<String, RasterImage> res = new HashMap<>();
		var list2d = split2D(w, h);
		for (int y = 0; y < list2d.size(); y++) {
			var list = list2d.get(y);
			for (int x = 0; x < list.size(); x++) {
				res.put(nameMapper.apply(x, y), list.get(x));
			}
		}
		return res;
	}

	public static RasterImage screenShot(Rectangle r) throws GraphicsException {
		try {
			return new RasterImage(new Robot().createScreenCapture(r));
		} catch (AWTException ex) {
			throw new GraphicsException(ex);
		}
	}

	public RasterImage tiling(int xNum, int yNum) {
		return tiling(xNum, yNum, getWidth(), getHeight());
	}

	public RasterImage tilingX(int xNum) {
		return tiling(xNum, 1, getWidth(), getHeight());
	}

	public RasterImage tiling(int xNum, int yNum, int drawW, int drawH) {
		RasterImage dst = new RasterImage(xNum * drawW, yNum * drawH);
		Graphics2D g2 = dst.createGraphics2D();
		for (int y = 0; y < yNum; y++) {
			for (int x = 0; x < xNum; x++) {
				g2.drawImage(this.image, x * drawW, y * drawH, drawW, drawH, null);
			}
		}
		g2.dispose();
		return dst;
	}

	public RasterImage subImage(int x, int y, int w, int h) {
		return new RasterImage(image.getSubimage(x, y, w, h));
	}

	public RasterImage subImage(Point2D p, Dimension2D d) {
		return subImage((int) p.getX(), (int) p.getY(), (int) d.getWidth(), (int) d.getHeight());
	}

	public RasterImage subImage(Shape r) {
		return subImage(r.getBounds().x, r.getBounds().y,
				r.getBounds().width, r.getBounds().height);
	}

	//pがtrueになる最も左上から最も右下が返される。
	public RasterImage autoTrimming(Predicate<Pixel> p) {
		Point start = start(this, p);
		Point end = end(this, p);
		int w = end.x - start.x;
		int h = end.y - start.y;
		if (w <= 0 || h <= 0) {
			return null;
		}
		return subImage(start.x, start.y, w, h);
	}

	public static final Predicate<Pixel> ALPHA_IS_TRANSPARENT = (v) -> v.getA() == ARGBColor.ALPHA_TRANSPARENT;
	public static final Predicate<Pixel> ALPHA_IS_OPAQUE = (v) -> v.getA() == ARGBColor.ALPHA_OPAQUE;
	public static final Predicate<Pixel> ALPHA_IS_NOT_TRANSPARENT = (v) -> v.getA() != ARGBColor.ALPHA_TRANSPARENT;
	public static final Predicate<Pixel> ALPHA_IS_NOT_OPAQUE = (v) -> v.getA() != ARGBColor.ALPHA_OPAQUE;

	private static Point start(RasterImage src, Predicate<Pixel> p) {
		Raster raster = src.asRaster();
		Pixel[][] pix = raster.all();
		Point res = new Point();
		for (int y = 0; y < pix.length; y++) {
			if (Stream.of(pix[y]).anyMatch(p)) {
				res.y = y;
				break;
			}
		}
		pix = raster.rotate(90).all();
		for (int y = 0; y < pix.length; y++) {
			if (Stream.of(pix[y]).anyMatch(p)) {
				res.x = y;
				break;
			}
		}
		return res;
	}

	private static Point end(RasterImage src, Predicate<Pixel> p) {
		Raster raster = src.asRaster();
		Pixel[][] pix = raster.all();
		Point res = new Point();
		for (int y = pix.length - 1; y >= 0; y--) {
			if (Stream.of(pix[y]).anyMatch(p)) {
				res.y = y;
				break;
			}
		}
		pix = raster.rotate(90).all();
		for (int y = pix.length - 1; y >= 0; y--) {
			if (Stream.of(pix[y]).anyMatch(p)) {
				res.x = y;
				break;
			}
		}
		return res;
	}

	public static RasterImage concatX(RasterImage... i) {
		return concatX(Arrays.asList(i));
	}

	public static RasterImage concatX(List<RasterImage> i) {
		if (i.isEmpty()) {
			throw new IllegalArgumentException("images is empty : images.length=[" + i.size() + "]");
		}
		int maxHeight = i.stream().mapToInt((p -> p.getHeight())).max().getAsInt();
		int width = i.stream().mapToInt(p -> p.getWidth()).sum();

		RasterImage res = new RasterImage(width, maxHeight);
		Graphics2D g = res.createGraphics2D(RenderingQuality.QUALITY);
		for (int n = 0, x = 0; n < i.size(); n++) {
			g.drawImage(i.get(n).image, x, 0, null);
			x += i.get(n).getWidth();
		}
		return res;
	}

	public boolean hasClaerPixel() {
		return asRaster().stream().anyMatch(p -> p.is完全透明());
	}

	public boolean hasOpauePixel() {
		return asRaster().stream().anyMatch(p -> p.is不透明());
	}

	public RasterImage replaceColor(Predicate<Pixel> b, UnaryOperator<Pixel> converter) {
		return asRaster().set(p -> {
			if (b.test(p)) {
				return converter.apply(p);
			}
			return p;
		}).newImage();
	}

	public RasterImage grayScale() {
		return asRaster().set(p -> p.average()).newImage();
	}

	public RasterImage weightedGrayScale() {
		Raster rs = asRaster();
		return rs.set(p -> {
			int r = (int) (p.getR() * 0.298912f);
			int g = (int) (p.getG() * 0.586611f);
			int b = (int) (p.getB() * 0.114478f);
			return rs.new Pixel(ARGBColor.toARGB(p.getA(), r, g, b));
		}).newImage();
	}

	public RasterImage sepia() {
		RasterImage i = grayScale();
		Raster rs = i.asRaster();
		return rs.set(p -> {
			int r = (int) (p.getR() * 1.12f);
			if (r > 255) {
				r = 255;
			}
			int g = (int) (p.getG() * 0.66f);
			int b = (int) (p.getB() * 0.20f);
			return rs.new Pixel(ARGBColor.toARGB(p.getA(), r, g, b));
		}).updateImage();
	}

	public RasterImage monochrome(int center) {
		Raster rs = asRaster();
		return rs.set(p -> {
			int a = p.average().value;
			if (a > center) {
				return rs.new Pixel(ARGBColor.WHITE);
			} else {
				return rs.new Pixel(ARGBColor.BLACK);
			}
		}).newImage();
	}

	public RasterImage reverseColor() {
		return asRaster().set(p -> p.reverse()).newImage();
	}

	public RasterImage mosaic(int size) throws GraphicsException {
		BufferedImage src = this.image;
		if (size < 1) {
			throw new IllegalArgumentException("size < 1 : size=[" + size + "]");
		}
		if (size > src.getWidth() || size > src.getHeight()) {
			throw new GraphicsException("size is over image bounds : size=[" + size + "]");
		}
		BufferedImage dst = ImageUtil.copy(src);
		int[][] pix = getPixel2D(src);
		for (int y = 0, imageHeight = src.getHeight(); y < imageHeight; y += size) {
			for (int x = 0, imageWidth = src.getWidth(); x < imageWidth; x += size) {
				int argb = pix[y][x];
				for (int mosaicY = y, i = 0; i < size; mosaicY++, i++) {
					for (int mosaicX = x, j = 0; j < size; mosaicX++, j++) {
						if (mosaicX <= imageWidth && mosaicY <= imageHeight) {
							pix[mosaicY][mosaicX] = argb;
						}
					}
				}
			}
		}
		ImageUtil.setPixel2D(dst, pix);
		return new RasterImage(dst);
	}

	public RasterImage rotate(float angle) {
		while (angle > 360) {
			angle -= 360;
		}
		while (angle < 0) {
			angle += 360;
		}
		if (angle == 0) {
			return clone();
		}
		RasterImage dst = clone();
		Graphics2D g = dst.createGraphics2D(RenderingQuality.QUALITY);
		g.setClip(0, 0, dst.getWidth(), dst.getHeight());
		g.setColor(new Color(ARGBColor.CLEAR_BLACK, true));
		g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
		g.rotate(Math.toRadians(angle), dst.getWidth() / 2, dst.getHeight() / 2);
		g.drawImage(this.image, 0, 0, null);
		g.dispose();
		return dst;
	}

	public RasterImage addAlpha(int a) {
		return asRaster().set(p -> p.addA(a)).newImage();
	}

	public RasterImage mulAlpha(float a) {
		return asRaster().set(p -> p.mulA(a)).newImage();
	}

	public RasterImage rasterScroll(int[] shiftPixNum, int insertARGB) {
		BufferedImage dst = ImageUtil.copy(this.image);
		int[] sPix = shiftPixNum;
		if (shiftPixNum.length != dst.getHeight()) {
			sPix = new int[dst.getHeight()];
			for (int i = 0, spi = 0; i < sPix.length; i++) {
				sPix[i] = shiftPixNum[spi];
				spi = (spi < shiftPixNum.length - 1) ? spi + 1 : 0;
			}
		}
		int[][] pix = getPixel2D(dst);
		for (int y = 0; y < pix.length; y++) {
			if (sPix[y] == 0) {
				continue;
			}
			final int[] ROW = new int[pix[y].length];
			System.arraycopy(pix[y], 0, ROW, 0, ROW.length);
			int x = 0, lineIdx = 0;
			if (sPix[y] > 0) {
				Arrays.fill(pix[y], 0, sPix[y], insertARGB);
				x += sPix[y];
			} else {
				lineIdx += Math.abs(sPix[y]);
			}
			System.arraycopy(ROW, lineIdx, pix[y], x, ROW.length - Math.abs(sPix[y]));
			x += ROW.length - Math.abs(sPix[y]);
			Arrays.fill(pix[y], x, pix[y].length, insertARGB);
		}
		setPixel2D(dst, pix);
		return new RasterImage(dst);
	}

	public RasterImage resizeTo(float scale) {
		int w = (int) (getWidth() * scale);
		int h = (int) (getHeight() * scale);
		return resizeTo(w, h);
	}

	public RasterImage resizeTo(float wScale, float hScale) {
		int w = (int) (getWidth() * wScale);
		int h = (int) (getHeight() * hScale);
		return resizeTo(w, h);
	}

	public RasterImage resizeTo(Dimension2D d) {
		return resizeTo((int) d.getWidth(), (int) d.getHeight());
	}

	public RasterImage resizeTo(RasterImage i) {
		return resizeTo(i.getWidth(), i.getHeight());
	}

	public RasterImage resizeTo(int w, int h) {
		if (w == 0 || h == 0) {
			throw new IllegalArgumentException("resize image : size is 0");
		}
		if (getWidth() == w && getHeight() == h) {
			return clone();
		}
		RasterImage dst = new RasterImage(w, h);
		Graphics2D g2 = dst.createGraphics2D(RenderingQuality.QUALITY);
		g2.drawImage(this.image, 0, 0, w, h, null);
		g2.dispose();
		return dst;
	}

	public static List<RasterImage> resizeAll(float scale, RasterImage... images) {
		return resizeAll(scale, Arrays.asList(images));
	}

	public static List<RasterImage> resizeAll(float scale, List<RasterImage> images) {
		return resizeAll(scale, scale, images);
	}

	public static List<RasterImage> resizeAll(float wScale, float hScale, RasterImage... images) {
		return resizeAll(wScale, hScale, Arrays.asList(images));
	}

	public static List<RasterImage> resizeAll(float wScale, float hScale, List<RasterImage> images) {
		List<RasterImage> res = new ArrayList<>();
		for (var v : images) {
			res.add(v.resizeTo(wScale, hScale));
		}
		return res;
	}

	public static List<RasterImage> resizeAll(Dimension2D d, RasterImage... images) {
		return resizeAll(d, Arrays.asList(images));
	}

	public static List<RasterImage> resizeAll(Dimension2D d, List<RasterImage> images) {
		return resizeAll((int) d.getWidth(), (int) d.getHeight(), images);
	}

	public static List<RasterImage> resizeAll(int w, int h, RasterImage... images) {
		return resizeAll(w, h, Arrays.asList(images));
	}

	public static List<RasterImage> resizeAll(int w, int h, List<RasterImage> images) {
		List<RasterImage> res = new ArrayList<>();
		for (var v : images) {
			res.add(v.resizeTo(w, h));
		}
		return res;
	}

	public List<RasterImage> nCopies(int n) {
		List<RasterImage> res = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			res.add(clone());
		}
		return res;
	}

}

/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2018 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imagej.legacy.translate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;

public class ImagePlusCreatorTest
{

	private Context context;
	private GrayImagePlusCreator creator;
	private DatasetService datasetService;

	@Before
	public void setUp() {
		context = new Context();
		creator = new GrayImagePlusCreator( context );
		datasetService = context.service( DatasetService.class );
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		creator = null;
		datasetService = null;
	}

	@Test
	public void testBytes() {
		testArrayImage( UnsignedByteType::new, ByteProcessor.class );
	}

	@Test
	public void testFloats() {
		testArrayImage( FloatType::new, FloatProcessor.class );
	}

	@Test
	public void testShorts() {
		testArrayImage( UnsignedShortType::new, ShortProcessor.class );
	}

	@Ignore("currently broken")
	@Test
	public void testSignedShorts() {
		testArrayImage( ShortType::new, FloatProcessor.class );
	}

	@Test
	public void testByteCellImage() {
		testCellImage( UnsignedByteType::new, ByteProcessor.class );
	}

	@Test
	public void testFloatCellImage() {
		testCellImage( FloatType::new, FloatProcessor.class );
	}

	@Test
	public void testShortCellImage() {
		testCellImage( UnsignedShortType::new, ShortProcessor.class );
	}

	@Test
	public void testBytePlanarImage() {
		testPlanarImage( UnsignedByteType::new, ByteProcessor.class );
	}

	@Test
	public void testFloatPlanarImage() {
		testPlanarImage( FloatType::new, FloatProcessor.class );
	}

	@Test
	public void testShortPlanarImage() {
		testPlanarImage( UnsignedShortType::new, ShortProcessor.class );
	}

	@Test
	public void testColor() {
		Img< UnsignedByteType > image = ArrayImgs.unsignedBytes( byteRange(1, 12), 2, 2, 3);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.CHANNEL };
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( image, "image", axes );
		ColorImagePlusCreator creator = new ColorImagePlusCreator( context );
		Dataset ds = datasetService.create( imgPlus );
		ds.setRGBMerged( true );
		ImagePlus imagePlus = creator.createLegacyImage( ds );
		ImageProcessor processor = imagePlus.getProcessor();
		assertTrue( processor instanceof ColorProcessor );
		assertEquals( 0xff010509, processor.getPixel(0,0) );
		assertEquals( 0xff02060a, processor.getPixel(1,0) );
		assertEquals( 0xff03070b, processor.getPixel(0,1) );
		assertEquals( 0xff04080c, processor.getPixel(1,1) );
	}

	@Test
	public void testColor2() {
		Img< UnsignedByteType > image = ArrayImgs.unsignedBytes( byteRange(1, 12), 2, 1, 3, 2);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z };
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( image, "image", axes );
		ColorImagePlusCreator creator = new ColorImagePlusCreator( context );
		Dataset ds = datasetService.create( imgPlus );
		ds.setRGBMerged( true );
		ImagePlus imagePlus = creator.createLegacyImage( ds );
		ImageProcessor processor = imagePlus.getStack().getProcessor( 1 );
		assertTrue( processor instanceof ColorProcessor );
		assertEquals( 0xff010305, processor.getPixel(0,0) );
		assertEquals( 0xff020406, processor.getPixel(1,0) );
		processor = imagePlus.getStack().getProcessor( 2 );
		assertEquals( 0xff07090b, processor.getPixel(0,0) );
		assertEquals( 0xff080a0c, processor.getPixel(1,0) );
	}

	@Test
	public void testColor3() {
		Img< UnsignedByteType > image = ArrayImgs.unsignedBytes( byteRange(1, 12), 2, 1, 2, 3);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL };
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( image, "image", axes );
		ColorImagePlusCreator creator = new ColorImagePlusCreator( context );
		Dataset ds = datasetService.create( imgPlus );
		ds.setRGBMerged( true );
		ImagePlus imagePlus = creator.createLegacyImage( ds );
		ImageProcessor processor = imagePlus.getStack().getProcessor( 1 );
		assertTrue( processor instanceof ColorProcessor );
		assertEquals( 0xff010509, processor.getPixel(0,0) );
		assertEquals( 0xff02060a, processor.getPixel(1,0) );
		processor = imagePlus.getStack().getProcessor( 2 );
		assertEquals( 0xff03070b, processor.getPixel(0,0) );
		assertEquals( 0xff04080c, processor.getPixel(1,0) );
	}

	private byte[] byteRange( int from, int to )
	{
		byte[] result = new byte[ to - from + 1 ];
		for ( int i = 0; i < result.length; i++ )
			result[i] = ( byte ) (from + i);
		return result;
	}

	@Test
	public void testCompositeImage() {
		byte[] values = new byte[] { 1, 2, 3, 4 };
		Img< UnsignedByteType > image = ArrayImgs.unsignedBytes(values, 2, 1, 2);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.CHANNEL };
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( image, "image", axes );
		imgPlus.setCompositeChannelCount( 7 );
		Dataset ds = datasetService.create( imgPlus );
		assertTrue( LegacyUtils.isColorCompatible( ds ) );
		ColorImagePlusCreator creator = new ColorImagePlusCreator( context );
		ImagePlus imagePlus = creator.createLegacyImage( ds );
		assertTrue( imagePlus instanceof CompositeImage );
		ImageProcessor processor = imagePlus.getProcessor();
		assertArrayEquals( Arrays.copyOf( values, 2 ) , ((byte[]) processor.getPixels()) );
	}

	@Ignore("not working yet")
	@Test
	public void testCompositeImageLong() {
		long[] values = { 1, 2, 3, 4 };
		Img< LongType > image = ArrayImgs.longs(values, 2, 1, 2);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.CHANNEL };
		ImgPlus< LongType > imgPlus = new ImgPlus<>( image, "image", axes );
		imgPlus.setCompositeChannelCount( 7 );
		Dataset ds = datasetService.create( imgPlus );
		assertTrue( LegacyUtils.isColorCompatible( ds ) );
		ColorImagePlusCreator creator = new ColorImagePlusCreator( context );
		ImagePlus imagePlus = creator.createLegacyImage( ds );
		assertTrue( imagePlus instanceof CompositeImage );
	}

	private < T extends NativeType<T> & RealType<T> > void testArrayImage( Supplier< T > typeConstructor, Class< ? > processorClass )
	{
		testImgPlusCreator( typeConstructor, processorClass, new ArrayImgFactory<>() );
	}

	private < T extends NativeType<T> & RealType<T> > void testCellImage( Supplier< T > typeConstructor, Class< ? > processorClass )
	{
		testImgPlusCreator( typeConstructor, processorClass, new CellImgFactory<>() );
	}

	private < T extends NativeType<T> & RealType<T> > void testPlanarImage( Supplier< T > typeConstructor, Class< ? > processorClass )
	{
		testImgPlusCreator( typeConstructor, processorClass, new PlanarImgFactory<>() );
	}

	private < T extends NativeType< T > & RealType< T > > void testImgPlusCreator( Supplier< T > typeConstructor, Class< ? > processorClass, ImgFactory< T > factory )
	{
		float[] expectedPixels = { 1, 2, 3, 4 };
		long[] dimensions = { 2, 2 };
		Img< T > image = createImg( typeConstructor, expectedPixels, dimensions, factory );
		ImagePlus imagePlus = creator.createLegacyImage( datasetService.create( image ) );
		ImageProcessor processor = imagePlus.getProcessor();
		assertTrue( processorClass.isInstance( processor ) );
		float[] actualPixels = pixelsAsFloatArray( processor );
		assertArrayEquals( expectedPixels, actualPixels, 0f);
	}

	private float[] pixelsAsFloatArray( ImageProcessor processor )
	{
		return ( float[] ) processor.toFloat( 0, null ).getPixels();
	}

	private < T extends NativeType< T > & RealType< T > > Img< T > createImg( Supplier< T > aNew, float[] expectedPixels, long[] dimensions, ImgFactory< T > factory )
	{
		Img< T > image = factory.create( aNew, dimensions );
		int i = 0;
		for( T pixel : image)
			pixel.setReal( expectedPixels[i++] );
		return image;
	}
}

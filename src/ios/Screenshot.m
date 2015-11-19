//
// Screenshot.h
//
// Created by Simon Madine on 29/04/2010.
// Copyright 2010 The Angry Robot Zombie Factory.
// - Converted to Cordova 1.6.1 by Josemando Sobral.
// MIT licensed
//
// Modifications to support orientation change by @ffd8
//

#import <Cordova/CDV.h>
#import "Screenshot.h"

@implementation Screenshot

@synthesize webView;

- (UIImage *)getScreenshot
{
	UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
	CGRect rect = [keyWindow bounds];
	UIGraphicsBeginImageContextWithOptions(rect.size, YES, 0);
	[keyWindow drawViewHierarchyInRect:keyWindow.bounds afterScreenUpdates:YES];
	UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
	UIGraphicsEndImageContext();
	return img;
}

- (void)saveScreenshot:(CDVInvokedUrlCommand*)command
{
	NSString *filename = [command.arguments objectAtIndex:2];
	NSNumber *quality = [command.arguments objectAtIndex:1];

	NSString *path = [NSString stringWithFormat:@"%@.jpg",filename];
	NSString *jpgPath = [NSTemporaryDirectory() stringByAppendingPathComponent:path];

	UIImage *oldImage = [self getScreenshot];

	CGRect newRect = [self cropRectForImage:oldImage];
	NSLog(@"newRect: %@", newRect);
	
	CGImageRef imageRef = CGImageCreateWithImageInRect(oldImage.CGImage, newRect);
	UIImage *image = [UIImage imageWithCGImage:imageRef];
	CGImageRelease(imageRef);

	NSData *imageData = UIImageJPEGRepresentation(image,[quality floatValue]);
	[imageData writeToFile:jpgPath atomically:NO];

	CDVPluginResult* pluginResult = nil;
	NSDictionary *jsonObj = [ [NSDictionary alloc]
		initWithObjectsAndKeys :
		jpgPath, @"filePath",
		@"true", @"success",
		nil
	];

	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
	[self writeJavascript:[pluginResult toSuccessCallbackString:command.callbackId]];
}

- (void) getScreenshotAsURI:(CDVInvokedUrlCommand*)command
{
	NSNumber *quality = command.arguments[0];
	UIImage *image = [self getScreenshot];
	NSData *imageData = UIImageJPEGRepresentation(image,[quality floatValue]);
	NSString *base64Encoded = [imageData base64EncodedStringWithOptions:0];
	NSDictionary *jsonObj = @{
	    @"URI" : [NSString stringWithFormat:@"data:image/jpeg;base64,%@", base64Encoded]
	};
	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}

- (CGRect)cropRectForImage:(UIImage *)image {
	CGImageRef cgImage = image.CGImage;
	CGContextRef context = [self createARGBBitmapContextFromImage:cgImage];
	if (context == NULL) return CGRectZero; 
	
	size_t width = CGImageGetWidth(cgImage);
	size_t height = CGImageGetHeight(cgImage);
	CGRect rect = CGRectMake(0, 0, width, height);
	
	CGContextDrawImage(context, rect, cgImage);
	
	unsigned char *data = CGBitmapContextGetData(context);
	CGContextRelease(context);
	
	//Filter through data and look for non-transparent pixels.
	int lowX = width;
	int lowY = height;
	int highX = 0;
	int highY = 0;
	if (data != NULL) {
	    for (int y=0; y<height; y++) {
	        for (int x=0; x<width; x++) {
	            int pixelIndex = (width * y + x) * 4 /* 4 for A, R, G, B */;
	            if (data[pixelIndex] != 255) { //Alpha value is not zero; pixel is not transparent.
	                if (x < lowX) lowX = x;
	                if (x > highX) highX = x;
	                if (y < lowY) lowY = y;
	                if (y > highY) highY = y;
	            }
	        }
	    }
	    free(data);
	} else {
	    return CGRectZero;
	}
	
	return CGRectMake(lowX, lowY, highX-lowX, highY-lowY);
}

- (CGContextRef)createARGBBitmapContextFromImage:(CGImageRef)inImage {
	CGContextRef context = NULL;
	CGColorSpaceRef colorSpace;
	void *bitmapData;
	int bitmapByteCount;
	int bitmapBytesPerRow;
	
	// Get image width, height. We'll use the entire image.
	size_t width = CGImageGetWidth(inImage);
	size_t height = CGImageGetHeight(inImage);
	
	// Declare the number of bytes per row. Each pixel in the bitmap in this
	// example is represented by 4 bytes; 8 bits each of red, green, blue, and
	// alpha.
	bitmapBytesPerRow = (width * 4);
	bitmapByteCount = (bitmapBytesPerRow * height);
	
	// Use the generic RGB color space.
	colorSpace = CGColorSpaceCreateDeviceRGB();
	if (colorSpace == NULL) return NULL;
	
	// Allocate memory for image data. This is the destination in memory
	// where any drawing to the bitmap context will be rendered.
	bitmapData = malloc( bitmapByteCount );
	if (bitmapData == NULL)
	{
	    CGColorSpaceRelease(colorSpace);
	    return NULL;
	}
	
	// Create the bitmap context. We want pre-multiplied ARGB, 8-bits
	// per component. Regardless of what the source image format is
	// (CMYK, Grayscale, and so on) it will be converted over to the format
	// specified here by CGBitmapContextCreate.
	context = CGBitmapContextCreate (bitmapData,
	                                 width,
	                                 height,
	                                 8,      // bits per component
	                                 bitmapBytesPerRow,
	                                 colorSpace,
	                                 kCGImageAlphaPremultipliedFirst);
	if (context == NULL) free (bitmapData);
	
	// Make sure and release colorspace before returning
	CGColorSpaceRelease(colorSpace);
	
	return context;
}
@end

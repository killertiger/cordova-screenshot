//
//  UIImage+Trim.h
//  Separados no Nascimento
//
//  Created by Bruno Oliveira on 11/19/15.
//
//

#import <UIKit/UIKit.h>

@interface UIImage (Trim)

- (UIEdgeInsets)transparencyInsetsRequiringFullOpacity:(BOOL)fullyOpaque;
- (UIEdgeInsets)transparencyInsetsByCuttingWhitespace:(UInt8)tolerance;
- (UIImage *)imageByTrimmingTransparentPixels;
- (UIImage *)imageByTrimmingTransparentPixelsRequiringFullOpacity:(BOOL)fullyOpaque;
- (UIImage *)imageByTrimmingWhitePixelsWithOpacity:(UInt8)tolerance;

- (UIImage *)imageWithImage:(CGFloat)width maxHeight:(CGFloat)height;

@end

/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
*/

package com.edmodo.cropper.cropwindow.edge;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.edmodo.cropper.util.AspectRatioUtil;

/**
 * Enum representing an edge in the crop window.
 */
public enum Edge {

    LEFT,
    TOP,
    RIGHT,
    BOTTOM;

    // Private Constants ///////////////////////////////////////////////////////////////////////////

    // Minimum distance in pixels that one edge can get to its opposing edge.
    // This is an arbitrary value that simply prevents the crop window from becoming too small.
    public static final int MIN_CROP_LENGTH_PX = 40;

    // Member Variables ////////////////////////////////////////////////////////////////////////////

    // The coordinate value of this edge.
    // This will be the x-coordinate for LEFT and RIGHT edges and the y-coordinate for TOP and BOTTOM edges.
    private float mCoordinate;

    // Public Methods //////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the coordinate of the Edge. The coordinate will represent the x-coordinate for LEFT and
     * RIGHT Edges and the y-coordinate for TOP and BOTTOM edges.
     *
     * @param coordinate the position of the edge
     */
    public void setCoordinate(float coordinate) {
        mCoordinate = coordinate;
    }

    /**
     * Add the given number of pixels to the current coordinate position of this Edge.
     *
     * @param distance the number of pixels to add
     */
    public void offset(float distance) {
        mCoordinate += distance;
    }

    /**
     * Gets the coordinate of the Edge
     *
     * @return the Edge coordinate (x-coordinate for LEFT and RIGHT Edges and the y-coordinate for
     * TOP and BOTTOM edges)
     */
    public float getCoordinate() {
        return mCoordinate;
    }

    /**
     * Sets the Edge to the given x-y coordinate but also adjusting for snapping to the image bounds
     * and parent view border constraints.
     *
     * @param x               the x-coordinate
     * @param y               the y-coordinate
     * @param imageRect       the bounding rectangle of the image
     * @param imageSnapRadius the radius (in pixels) at which the edge should snap to the image
     */
    public void adjustCoordinate(float x, float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        switch (this) {
            case LEFT:
                mCoordinate = adjustLeft(x, imageRect, imageSnapRadius, aspectRatio);
                break;
            case TOP:
                mCoordinate = adjustTop(y, imageRect, imageSnapRadius, aspectRatio);
                break;
            case RIGHT:
                mCoordinate = adjustRight(x, imageRect, imageSnapRadius, aspectRatio);
                break;
            case BOTTOM:
                mCoordinate = adjustBottom(y, imageRect, imageSnapRadius, aspectRatio);
                break;
        }
    }


    /**
     * Adjusts this Edge position such that the resulting window will have the given aspect ratio.
     *
     * @param aspectRatio the aspect ratio to achieve
     */
    public void adjustCoordinate(float aspectRatio) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        switch (this) {
            case LEFT:
                mCoordinate = AspectRatioUtil.calculateLeft(top, right, bottom, aspectRatio);
                break;
            case TOP:
                mCoordinate = AspectRatioUtil.calculateTop(left, right, bottom, aspectRatio);
                break;
            case RIGHT:
                mCoordinate = AspectRatioUtil.calculateRight(left, top, bottom, aspectRatio);
                break;
            case BOTTOM:
                mCoordinate = AspectRatioUtil.calculateBottom(left, top, right, aspectRatio);
                break;
        }
    }

    /**
     * Returns whether or not you can re-scale the image based on whether any edge would be out of
     * bounds. Checks all the edges for a possibility of jumping out of bounds.
     *
     * @param edge        the Edge that is about to be expanded
     * @param imageRect   the rectangle of the picture
     * @param aspectRatio the desired aspectRatio of the picture
     *
     * @return whether or not the new image would be out of bounds.
     */
    public boolean isNewRectangleOutOfBounds(@NonNull Edge edge, @NonNull RectF imageRect, float aspectRatio) {

        final float offset = edge.snapOffset(imageRect);

        switch (this) {

            case LEFT:

                if (edge.equals(Edge.TOP)) {

                    final float top = imageRect.top;
                    final float bottom = Edge.BOTTOM.getCoordinate() - offset;
                    final float right = Edge.RIGHT.getCoordinate();
                    final float left = AspectRatioUtil.calculateLeft(top, right, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);

                } else if (edge.equals(Edge.BOTTOM)) {

                    final float bottom = imageRect.bottom;
                    final float top = Edge.TOP.getCoordinate() - offset;
                    final float right = Edge.RIGHT.getCoordinate();
                    final float left = AspectRatioUtil.calculateLeft(top, right, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);
                }
                break;

            case TOP:

                if (edge.equals(Edge.LEFT)) {

                    final float left = imageRect.left;
                    final float right = Edge.RIGHT.getCoordinate() - offset;
                    final float bottom = Edge.BOTTOM.getCoordinate();
                    final float top = AspectRatioUtil.calculateTop(left, right, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);

                } else if (edge.equals(Edge.RIGHT)) {

                    final float right = imageRect.right;
                    final float left = Edge.LEFT.getCoordinate() - offset;
                    final float bottom = Edge.BOTTOM.getCoordinate();
                    final float top = AspectRatioUtil.calculateTop(left, right, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);
                }
                break;

            case RIGHT:

                if (edge.equals(Edge.TOP)) {

                    final float top = imageRect.top;
                    final float bottom = Edge.BOTTOM.getCoordinate() - offset;
                    final float left = Edge.LEFT.getCoordinate();
                    final float right = AspectRatioUtil.calculateRight(left, top, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);

                } else if (edge.equals(Edge.BOTTOM)) {

                    final float bottom = imageRect.bottom;
                    final float top = Edge.TOP.getCoordinate() - offset;
                    final float left = Edge.LEFT.getCoordinate();
                    final float right = AspectRatioUtil.calculateRight(left, top, bottom, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);
                }
                break;

            case BOTTOM:

                if (edge.equals(Edge.LEFT)) {

                    final float left = imageRect.left;
                    final float right = Edge.RIGHT.getCoordinate() - offset;
                    final float top = Edge.TOP.getCoordinate();
                    final float bottom = AspectRatioUtil.calculateBottom(left, top, right, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);

                } else if (edge.equals(Edge.RIGHT)) {

                    final float right = imageRect.right;
                    final float left = Edge.LEFT.getCoordinate() - offset;
                    final float top = Edge.TOP.getCoordinate();
                    final float bottom = AspectRatioUtil.calculateBottom(left, top, right, aspectRatio);

                    return isOutOfBounds(top, left, bottom, right, imageRect);

                }
                break;
        }
        return true;
    }

    /**
     * Returns whether the new rectangle would be out of bounds.
     *
     * @param imageRect the Image to be compared with
     *
     * @return whether it would be out of bounds
     */
    private boolean isOutOfBounds(float top, float left, float bottom, float right, @NonNull RectF imageRect) {
        return (top < imageRect.top || left < imageRect.left || bottom > imageRect.bottom || right > imageRect.right);
    }

    /**
     * Snap this Edge to the given image boundaries.
     *
     * @param imageRect the bounding rectangle of the image to snap to
     *
     * @return the amount (in pixels) that this coordinate was changed (i.e. the new coordinate
     * minus the old coordinate value)
     */
    public float snapToRect(@NonNull RectF imageRect) {

        final float oldCoordinate = mCoordinate;

        switch (this) {
            case LEFT:
                mCoordinate = imageRect.left;
                break;
            case TOP:
                mCoordinate = imageRect.top;
                break;
            case RIGHT:
                mCoordinate = imageRect.right;
                break;
            case BOTTOM:
                mCoordinate = imageRect.bottom;
                break;
        }

        return mCoordinate - oldCoordinate;
    }

    /**
     * Returns the potential snap offset of snapToRect, without changing the coordinate.
     *
     * @param imageRect the bounding rectangle of the image to snap to
     *
     * @return the amount (in pixels) that this coordinate was changed (i.e. the new coordinate
     * minus the old coordinate value)
     */
    public float snapOffset(@NonNull RectF imageRect) {

        final float oldCoordinate = mCoordinate;
        final float newCoordinate;

        switch (this) {
            case LEFT:
                newCoordinate = imageRect.left;
                break;
            case TOP:
                newCoordinate = imageRect.top;
                break;
            case RIGHT:
                newCoordinate = imageRect.right;
                break;
            default: // BOTTOM
                newCoordinate = imageRect.bottom;
                break;
        }

        return newCoordinate - oldCoordinate;
    }

    /**
     * Gets the current width of the crop window.
     */
    public static float getWidth() {
        return Edge.RIGHT.getCoordinate() - Edge.LEFT.getCoordinate();
    }

    /**
     * Gets the current height of the crop window.
     */
    public static float getHeight() {
        return Edge.BOTTOM.getCoordinate() - Edge.TOP.getCoordinate();
    }

    /**
     * Determines if this Edge is outside the inner margins of the given bounding rectangle. The
     * margins come inside the actual frame by SNAPRADIUS amount; therefore, determines if the point
     * is outside the inner "margin" frame.
     */
    public boolean isOutsideMargin(@NonNull RectF rect, float margin) {

        final boolean result;

        switch (this) {
            case LEFT:
                result = mCoordinate - rect.left < margin;
                break;
            case TOP:
                result = mCoordinate - rect.top < margin;
                break;
            case RIGHT:
                result = rect.right - mCoordinate < margin;
                break;
            default: // BOTTOM
                result = rect.bottom - mCoordinate < margin;
                break;
        }
        return result;
    }

    // Private Methods /////////////////////////////////////////////////////////////////////////////

    /**
     * Get the resulting x-position of the left edge of the crop window given the handle's position
     * and the image's bounding box and snap radius.
     *
     * @param x               the x-position that the left edge is dragged to
     * @param imageRect       the bounding box of the image that is being cropped
     * @param imageSnapRadius the snap distance to the image edge (in pixels)
     *
     * @return the actual x-position of the left edge
     */
    private static float adjustLeft(float x, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        final float resultX;

        if (x - imageRect.left < imageSnapRadius) {

            resultX = imageRect.left;

        } else {

            // Select the minimum of the three possible values to use
            float resultXHoriz = Float.POSITIVE_INFINITY;
            float resultXVert = Float.POSITIVE_INFINITY;

            // Checks if the window is too small horizontally
            if (x >= Edge.RIGHT.getCoordinate() - MIN_CROP_LENGTH_PX) {
                resultXHoriz = Edge.RIGHT.getCoordinate() - MIN_CROP_LENGTH_PX;
            }
            // Checks if the window is too small vertically
            if (((Edge.RIGHT.getCoordinate() - x) / aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultXVert = Edge.RIGHT.getCoordinate() - (MIN_CROP_LENGTH_PX * aspectRatio);
            }
            resultX = Math.min(x, Math.min(resultXHoriz, resultXVert));
        }
        return resultX;
    }

    /**
     * Get the resulting x-position of the right edge of the crop window given the handle's position
     * and the image's bounding box and snap radius.
     *
     * @param x               the x-position that the right edge is dragged to
     * @param imageRect       the bounding box of the image that is being cropped
     * @param imageSnapRadius the snap distance to the image edge (in pixels)
     *
     * @return the actual x-position of the right edge
     */
    private static float adjustRight(float x, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        final float resultX;

        // If close to the edge...
        if (imageRect.right - x < imageSnapRadius) {

            resultX = imageRect.right;

        } else {

            // Select the maximum of the three possible values to use
            float resultXHoriz = Float.NEGATIVE_INFINITY;
            float resultXVert = Float.NEGATIVE_INFINITY;

            // Checks if the window is too small horizontally
            if (x <= Edge.LEFT.getCoordinate() + MIN_CROP_LENGTH_PX) {
                resultXHoriz = Edge.LEFT.getCoordinate() + MIN_CROP_LENGTH_PX;
            }
            // Checks if the window is too small vertically
            if (((x - Edge.LEFT.getCoordinate()) / aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultXVert = Edge.LEFT.getCoordinate() + (MIN_CROP_LENGTH_PX * aspectRatio);
            }
            resultX = Math.max(x, Math.max(resultXHoriz, resultXVert));
        }
        return resultX;
    }

    /**
     * Get the resulting y-position of the top edge of the crop window given the handle's position
     * and the image's bounding box and snap radius.
     *
     * @param y               the x-position that the top edge is dragged to
     * @param imageRect       the bounding box of the image that is being cropped
     * @param imageSnapRadius the snap distance to the image edge (in pixels)
     *
     * @return the actual y-position of the top edge
     */
    private static float adjustTop(float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        final float resultY;

        if (y - imageRect.top < imageSnapRadius) {

            resultY = imageRect.top;

        } else {

            // Select the minimum of the three possible values to use
            float resultYVert = Float.POSITIVE_INFINITY;
            float resultYHoriz = Float.POSITIVE_INFINITY;

            // Checks if the window is too small vertically
            if (y >= Edge.BOTTOM.getCoordinate() - MIN_CROP_LENGTH_PX)
                resultYHoriz = Edge.BOTTOM.getCoordinate() - MIN_CROP_LENGTH_PX;

            // Checks if the window is too small horizontally
            if (((Edge.BOTTOM.getCoordinate() - y) * aspectRatio) <= MIN_CROP_LENGTH_PX)
                resultYVert = Edge.BOTTOM.getCoordinate() - (MIN_CROP_LENGTH_PX / aspectRatio);

            resultY = Math.min(y, Math.min(resultYHoriz, resultYVert));
        }
        return resultY;
    }

    /**
     * Get the resulting y-position of the bottom edge of the crop window given the handle's
     * position and the image's bounding box and snap radius.
     *
     * @param y               the x-position that the bottom edge is dragged to
     * @param imageRect       the bounding box of the image that is being cropped
     * @param imageSnapRadius the snap distance to the image edge (in pixels)
     *
     * @return the actual y-position of the bottom edge
     */
    private static float adjustBottom(float y, @NonNull RectF imageRect, float imageSnapRadius, float aspectRatio) {

        final float resultY;

        if (imageRect.bottom - y < imageSnapRadius) {

            resultY = imageRect.bottom;

        } else {

            // Select the maximum of the three possible values to use
            float resultYVert = Float.NEGATIVE_INFINITY;
            float resultYHoriz = Float.NEGATIVE_INFINITY;

            // Checks if the window is too small vertically
            if (y <= Edge.TOP.getCoordinate() + MIN_CROP_LENGTH_PX) {
                resultYVert = Edge.TOP.getCoordinate() + MIN_CROP_LENGTH_PX;
            }
            // Checks if the window is too small horizontally
            if (((y - Edge.TOP.getCoordinate()) * aspectRatio) <= MIN_CROP_LENGTH_PX) {
                resultYHoriz = Edge.TOP.getCoordinate() + (MIN_CROP_LENGTH_PX / aspectRatio);
            }
            resultY = Math.max(y, Math.max(resultYHoriz, resultYVert));
        }
        return resultY;
    }
}

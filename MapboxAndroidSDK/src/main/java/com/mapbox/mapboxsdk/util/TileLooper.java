package com.mapbox.mapboxsdk.util;

import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.tileprovider.MapTile;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

    protected final Point mUpperLeft = new Point();
    protected final Point mLowerRight = new Point();
    protected final Point center = new Point();

    public final void loop(final Canvas pCanvas, final int pZoomLevel, final int pTileSizePx, final Rect pViewPort) {
        // Calculate the amount of tiles needed for each side around the center one.
        TileSystem.PixelXYToTileXY(pViewPort.left, pViewPort.top, mUpperLeft);
        mUpperLeft.offset(-1, -1);
        TileSystem.PixelXYToTileXY(pViewPort.right, pViewPort.bottom, mLowerRight);
        center.set((mUpperLeft.x + mLowerRight.x)/2, (mUpperLeft.y + mLowerRight.y)/2);
        final int mapTileUpperBound = 1 << pZoomLevel;
        ArrayList<Point> orderedList = new ArrayList<Point>();
        initializeLoop(pZoomLevel, pTileSizePx);

        /**
         * TO DO - there is definitely a more efficient way of doing this
         */
        for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
            for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
                orderedList.add(new Point(x,y));
            }
        }
        Collections.sort(orderedList, new ClosenessToCenterComparator());
        for (Point point: orderedList) {
            final int tileY = GeometryMath.mod(point.y, mapTileUpperBound);
            final int tileX = GeometryMath.mod(point.x, mapTileUpperBound);
            final MapTile tile = new MapTile(pZoomLevel, tileX, tileY);
            handleTile(pCanvas, pTileSizePx, tile, point.x, point.y);
        }

        finalizeLoop();
    }

    /**
     * Compares two points for which one is closer to the center
     */
    protected class ClosenessToCenterComparator implements Comparator<Point>{
        @Override
        public int compare(Point one, Point two) {
            float oneLength = length(one);
            float twoLength = length(two);
            if (oneLength > twoLength) {
                return 1;
            } else if (oneLength < twoLength) {
                return -1;
            } else {
                return 0;
            }
        }
        private float length(Point point) {
            return (float) (Math.sqrt(Math.pow(point.x - center.x, 2) +
                    Math.pow(point.y - center.y, 2)));
        }
    }

    public abstract void initializeLoop(int pZoomLevel, int pTileSizePx);

    public abstract void handleTile(Canvas pCanvas, int pTileSizePx, MapTile pTile, int pX, int pY);

    public abstract void finalizeLoop();
}

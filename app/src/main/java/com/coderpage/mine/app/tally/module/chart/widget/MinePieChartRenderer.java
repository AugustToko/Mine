package com.coderpage.mine.app.tally.module.chart.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * @author lc. 2018-10-10 09:30
 * @since 0.6.0
 */

public class MinePieChartRenderer extends PieChartRenderer {

    /**
     * paint object used for drwing the slice-text
     */
    private Paint mEntryLabelsPaint;

    public MinePieChartRenderer(PieChart chart, ChartAnimator animator,
                                ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);

        mEntryLabelsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEntryLabelsPaint.setColor(Color.WHITE);
        mEntryLabelsPaint.setTextAlign(Paint.Align.CENTER);
        mEntryLabelsPaint.setTextSize(Utils.convertDpToPixel(13f));
    }

    @Override
    public void drawValues(Canvas c) {
        MPPointF center = mChart.getCenterCircleBox();

        // get whole the radius
        float radius = mChart.getRadius();
        float rotationAngle = mChart.getRotationAngle();
        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        final float holeRadiusPercent = mChart.getHoleRadius() / 100.f;
        float labelRadiusOffset = radius / 10f * 3.6f;

        if (mChart.isDrawHoleEnabled()) {
            labelRadiusOffset = (radius - (radius * holeRadiusPercent)) / 2f;
        }

        final float labelRadius = radius - labelRadiusOffset;

        PieData data = mChart.getData();
        List<IPieDataSet> dataSets = data.getDataSets();

        float yValueSum = data.getYValueSum();

        boolean drawEntryLabels = mChart.isDrawEntryLabelsEnabled();

        float angle;
        int xIndex = 0;

        c.save();

        float offset = Utils.convertDpToPixel(5.f);

        for (int i = 0; i < dataSets.size(); i++) {

            IPieDataSet dataSet = dataSets.get(i);

            final boolean drawValues = dataSet.isDrawValuesEnabled();

            if (!drawValues && !drawEntryLabels)
                continue;

            applyValueTextStyle(dataSet);

            float lineHeight = Utils.calcTextHeight(mValuePaint, "Q")
                    + Utils.convertDpToPixel(4f);

            IValueFormatter formatter = dataSet.getValueFormatter();

            int entryCount = dataSet.getEntryCount();

            mValueLinePaint.setColor(dataSet.getValueLineColor());
            mValueLinePaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getValueLineWidth()));

            final Paint.FontMetrics valueFontMetrics = mValueLinePaint.getFontMetrics();
            final float sliceSpace = getSliceSpace(dataSet);

            // 上一个绘制的 label 文字的 top&bottom。用于判断是否会导致文字覆盖情况
            // 区分绘制在饼状图的左面和右面
            float preLabelRightDrawTop = 0;
            float preLabelRightDrawBottom = 0;
            float preLabelLeftDrawTop = 0;
            float preLabelLeftDrawBottom = 0;

            for (int j = 0; j < entryCount; j++) {

                PieEntry entry = dataSet.getEntryForIndex(j);

                if (xIndex == 0)
                    angle = 0.f;
                else
                    angle = absoluteAngles[xIndex - 1] * phaseX;

                final float sliceAngle = drawAngles[xIndex];
                final float sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius);

                // offset needed to center the drawn text in the slice
                final float angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2.f) / 2.f;

                angle = angle + angleOffset;

                final float transformedAngle = rotationAngle + angle * phaseY;

                float value = mChart.isUsePercentValuesEnabled() ? entry.getY()
                        / yValueSum * 100f : entry.getY();

                final float sliceXBase = (float) Math.cos(transformedAngle * Utils.FDEG2RAD);
                final float sliceYBase = (float) Math.sin(transformedAngle * Utils.FDEG2RAD);

                final float valueLineLength1 = dataSet.getValueLinePart1Length();
                final float valueLineLength2 = dataSet.getValueLinePart2Length();
                final float valueLinePart1OffsetPercentage = dataSet.getValueLinePart1OffsetPercentage() / 100.f;

                float pt2x, pt2y;
                float labelPtx, labelPty;

                float line1Radius;

                if (mChart.isDrawHoleEnabled())
                    line1Radius = (radius - (radius * holeRadiusPercent))
                            * valueLinePart1OffsetPercentage
                            + (radius * holeRadiusPercent);
                else
                    line1Radius = radius * valueLinePart1OffsetPercentage;

                final float polyline2Width = dataSet.isValueLineVariableLength()
                        ? labelRadius * valueLineLength2 * (float) Math.abs(Math.sin(
                        transformedAngle * Utils.FDEG2RAD))
                        : labelRadius * valueLineLength2;

                final float pt0x = line1Radius * sliceXBase + center.x;
                final float pt0y = line1Radius * sliceYBase + center.y;

                final float pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x;
                final float pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y;

                final float valueTextTop = pt1y - (lineHeight / 2);
                final float valueTextBottom = pt1y + (lineHeight / 2);

                // label 绘制在饼状图的左面
                boolean labelOnLeftSide = transformedAngle % 360.0 >= 90.0 && transformedAngle % 360.0 <= 270.0;
                if (labelOnLeftSide) {
                    pt2x = pt1x - polyline2Width;
                    pt2y = pt1y;

                    mValuePaint.setTextAlign(Paint.Align.RIGHT);
                    mEntryLabelsPaint.setTextAlign(Paint.Align.RIGHT);

                    labelPtx = pt2x - offset;
                } else {
                    pt2x = pt1x + polyline2Width;
                    pt2y = pt1y;
                    mValuePaint.setTextAlign(Paint.Align.LEFT);
                    mEntryLabelsPaint.setTextAlign(Paint.Align.LEFT);

                    labelPtx = pt2x + offset;
                }
                // label 文字以标线未中心线绘制
                labelPty = (valueTextTop + valueTextBottom - valueFontMetrics.bottom - valueFontMetrics.top) / 2;

                // 当前文字的位置是否会覆盖上一个标签
                boolean willOverlayPreLabel;
                if (labelOnLeftSide) {
                    willOverlayPreLabel = (valueTextTop >= preLabelLeftDrawTop && valueTextTop <= preLabelLeftDrawBottom)
                            || (valueTextBottom >= preLabelLeftDrawTop && valueTextBottom <= preLabelLeftDrawBottom);
                } else {
                    willOverlayPreLabel = (valueTextTop >= preLabelRightDrawTop && valueTextTop <= preLabelRightDrawBottom)
                            || (valueTextBottom >= preLabelRightDrawTop && valueTextBottom <= preLabelRightDrawBottom);
                }

                // 当前的文字不会覆盖上一标签才会绘制
                if (!willOverlayPreLabel) {

                    int valueTextColor = dataSet.getValueTextColor(j);
                    mValueLinePaint.setColor(valueTextColor);

                    // 绘制标线
                    if (dataSet.getValueLineColor() != ColorTemplate.COLOR_NONE) {
                        c.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint);
                        c.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint);
                    }

                    // 绘制 label
                    String label = j < data.getEntryCount() && entry.getLabel() != null ? entry.getLabel() : "";
                    mValuePaint.setColor(valueTextColor);
                    c.drawText(label + " " + formatter.getFormattedValue(value, entry, 0, mViewPortHandler), labelPtx, labelPty, mValuePaint);

                    if (labelOnLeftSide) {
                        preLabelLeftDrawTop = valueTextTop;
                        preLabelLeftDrawBottom = valueTextBottom;
                    } else {
                        preLabelRightDrawTop = valueTextTop;
                        preLabelRightDrawBottom = valueTextBottom;
                    }
                }
                xIndex++;
            }
        }
        MPPointF.recycleInstance(center);
        c.restore();
    }
}

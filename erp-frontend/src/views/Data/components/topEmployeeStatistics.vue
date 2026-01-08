<script setup>
import {
  ref,
  watch,
  nextTick,
  onMounted,
  onBeforeUnmount,
  shallowRef,
} from "vue";

import * as echarts from "echarts";

defineOptions({
  name: "topEmployeeStatistics",
});

const props = defineProps({
  topEmployeeData: {
    type: Object,
    required: true,
  },
});

const chartInstance = shallowRef(null);
const chartRef = ref(null);

const initChart = () => {
  //use ref to get the dom instead of id
  const chartDom = chartRef.value;

  // If the DOM element does not exist, return directly.
  if (!chartDom) return;

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartDom);
  }

  const option = {
    tooltip: {
      trigger: "axis",
      backgroundColor: "#fff",
      borderRadius: 2,
      textStyle: {
        color: "#333",
        fontSize: 12,
        fontWeight: 300,
      },
    },

    grid: {
      top: "5%",
      left: "5%",
      right: "5%",
      bottom: "12%",
      containLabel: false,
    },

    xAxis: {
      show: false,
    },

    yAxis: {
      axisLine: {
        show: false,
      },

      axisTick: {
        show: false,
        alignWithLabel: true,
      },
      type: "category",

      axisLabel: {
        textStyle: {
          color: "#666",
          fontSize: "12px",
        },
      },

      data: props.topEmployeeData.nameList,
      inverse: true,
    },

    series: [
      {
        data: props.topEmployeeData.amountList,
        type: "bar", // bar chart
        showBackground: true,

        backgroundStyle: {
          color: "#F3F4F7",
        },
        barWidth: 20,
        barGap: "80%",
        barCategoryGap: "80%",

        itemStyle: {
          emphasis: {
            barBorderRadius: 30,
          },

          normal: {
            barBorderRadius: [0, 10, 10, 0],

            color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
              { offset: 0, color: "#FFBD00" },
              { offset: 1, color: "#FFD000" },
            ]),

            label: {
              show: true,
              formatter: "{@score}",
              color: "#333",
              position: ["8", "5"],
            },
          },
        },
      },
    ],
  };

  option && chartInstance.value.setOption(option);
};

watch(
  () => props.topEmployeeData,
  () => {
    // Use nextTick to ensure that the DOM update is complete before initialising the chart.
    nextTick(() => {
      initChart();
    });
  },
  { deep: true }
);

onMounted(() => {
  nextTick(() => {
    initChart();
  });

  window.addEventListener("resize", handleResize);
});

const handleResize = () => {
  chartInstance.value?.resize();
};

onBeforeUnmount(() => {
  if (chartInstance.value) {
    window.removeEventListener("resize", handleResize);
    chartInstance.value.dispose();
    chartInstance.value = null;
  }
});
</script>


<template>
  <div class="container">
    <h2 class="homeTitle">Top 5 Employee</h2>

    <div class="charBox">
      <div ref="chartRef" style="width: 100%; height: 320px"></div>
      <ul class="orderListLine turnover">
        <li>Contract Value(£)</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}

.homeTitle {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.charBox {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.orderListLine {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
}

.orderListLine li {
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.turnover {
  border-top: 1px solid #eee;
}
</style>
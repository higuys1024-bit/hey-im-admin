<template>
  <div class="app-container home">
    <!-- 统计指标卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #409EFF;">
              <el-icon size="24">
                <User />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">总用户量</div>
              <div class="statistics-value">{{ totalUserCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #67C23A;">
              <el-icon size="24">
                <ChatDotRound />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">总群组数量</div>
              <div class="statistics-value">{{ totalGroupCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #E6A23C;">
              <el-icon size="24">
                <Timer />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">日活用户</div>
              <div class="statistics-value">{{ dailyActiveCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #909399;">
              <el-icon size="24">
                <Clock />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">月活用户</div>
              <div class="statistics-value">{{ monthlyActiveCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #13C2C2;">
              <el-icon size="24">
                <Finished />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">今日签到用户数</div>
              <div class="statistics-value">{{ todayCheckinCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-icon" style="background-color: #722ED1;">
              <el-icon size="24">
                <Message />
              </el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-title">今日发消息用户数</div>
              <div class="statistics-value">{{ todayMsgUserCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计图表 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>用户注册</span>
              <el-select v-model="registrationSelectedDays" @change="loadRegistrationChartData"
                style="width: 100px; float: right;" size="small">
                <el-option label="7天" :value="7"></el-option>
                <el-option label="15天" :value="15"></el-option>
                <el-option label="30天" :value="30"></el-option>
              </el-select>
            </template>
            <div ref="registrationChartContainer" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>消息统计</span>
              <el-select v-model="selectedDays" @change="loadChartData" style="width: 100px; float: right;"
                size="small">
                <el-option label="7天" :value="7"></el-option>
                <el-option label="15天" :value="15"></el-option>
                <el-option label="30天" :value="30"></el-option>
              </el-select>
            </template>
            <div ref="chartContainer" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>每日签到用户数</span>
              <el-select v-model="checkinSelectedDays" @change="loadCheckinChartData"
                style="width: 100px; float: right;" size="small">
                <el-option label="7天" :value="7"></el-option>
                <el-option label="15天" :value="15"></el-option>
                <el-option label="30天" :value="30"></el-option>
              </el-select>
            </template>
            <div ref="checkinChartContainer" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>每日发消息用户数</span>
              <el-select v-model="msgUserSelectedDays" @change="loadMsgUserChartData"
                style="width: 100px; float: right;" size="small">
                <el-option label="7天" :value="7"></el-option>
                <el-option label="15天" :value="15"></el-option>
                <el-option label="30天" :value="30"></el-option>
              </el-select>
            </template>
            <div ref="msgUserChartContainer" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="Index" lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import { getDailyMessageCount } from '@/api/im/privateMessage';
import { getDailyGroupMessageCount } from '@/api/im/groupMessage';
import { getDailyRegistrationCount, getTotalUserCount, getActiveUserStats,
  getTodayCheckinUserCount, getDailyCheckinUserCount,
  getTodayMessageUserCount, getDailyMessageUserCount } from '@/api/im/user';
import { getTotalGroupCount } from '@/api/im/group';
import { User, ChatDotRound, Timer, Calendar, Clock, Finished, Message } from '@element-plus/icons-vue';

const chartContainer = ref<HTMLElement>();
const selectedDays = ref(7);
let chartInstance: echarts.ECharts | null = null;

const registrationChartContainer = ref<HTMLElement>();
const registrationSelectedDays = ref(7);
let registrationChartInstance: echarts.ECharts | null = null;

// 每日签到用户数图表
const checkinChartContainer = ref<HTMLElement>();
const checkinSelectedDays = ref(7);
let checkinChartInstance: echarts.ECharts | null = null;

// 每日发消息用户数图表
const msgUserChartContainer = ref<HTMLElement>();
const msgUserSelectedDays = ref(7);
let msgUserChartInstance: echarts.ECharts | null = null;

// 今日签到用户数 / 今日发消息用户数
const todayCheckinCount = ref(0);
const todayMsgUserCount = ref(0);

// 总用户数量
const totalUserCount = ref(0);

// 总群组数量
const totalGroupCount = ref(0);

// 活跃用户数据
const dailyActiveCount = ref(0);
const weeklyActiveCount = ref(0);
const monthlyActiveCount = ref(0);

// 加载总用户数量
const loadTotalUserCount = async () => {
  try {
    const response = await getTotalUserCount();
    totalUserCount.value = response.data;
  } catch (error) {
    console.error('加载总用户数量失败:', error);
    totalUserCount.value = 0;
  }
};

// 加载总群组数量
const loadTotalGroupCount = async () => {
  try {
    const response = await getTotalGroupCount();
    totalGroupCount.value = response.data;
  } catch (error) {
    console.error('加载总群组数量失败:', error);
    totalGroupCount.value = 0;
  }
};

// 加载活跃用户统计数据
const loadActiveUserStats = async () => {
  try {
    const response = await getActiveUserStats();
    dailyActiveCount.value = response.data.dailyActive;
    weeklyActiveCount.value = response.data.weeklyActive;
    monthlyActiveCount.value = response.data.monthlyActive;
  } catch (error) {
    console.error('加载活跃用户统计失败:', error);
    dailyActiveCount.value = 0;
    weeklyActiveCount.value = 0;
    monthlyActiveCount.value = 0;
  }
};


// 加载今日签到用户数
const loadTodayCheckinCount = async () => {
  try {
    const response = await getTodayCheckinUserCount();
    todayCheckinCount.value = response.data || 0;
  } catch (error) {
    console.error('加载今日签到用户数失败:', error);
    todayCheckinCount.value = 0;
  }
};

// 加载今日发消息用户数
const loadTodayMsgUserCount = async () => {
  try {
    const response = await getTodayMessageUserCount();
    todayMsgUserCount.value = response.data || 0;
  } catch (error) {
    console.error('加载今日发消息用户数失败:', error);
    todayMsgUserCount.value = 0;
  }
};

// 通用：将后端 [{date,count}] 数据按天补齐成连续 days 天的序列
const buildDailySeries = (data: any[], days: number) => {
  const dates: string[] = [];
  const counts: number[] = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    dates.push(dateStr);
    const dayData = (data || []).find(item => String(item.date).substring(0, 10) === dateStr);
    counts.push(dayData ? parseInt(dayData.count) : 0);
  }
  return { dates, counts };
};

// 加载图表数据
const loadChartData = async () => {
  try {
    // 同时加载私聊和群聊数据
    const [privateResponse, groupResponse] = await Promise.all([
      getDailyMessageCount(selectedDays.value),
      getDailyGroupMessageCount(selectedDays.value)
    ]);

    const privateData = privateResponse.data;
    const groupData = groupResponse.data;

    // 处理数据，确保所有日期都有数据
    const chartData = processChartData(privateData, groupData);

    // 更新图表
    if (chartInstance) {
      chartInstance.setOption({
        xAxis: {
          data: chartData.dates
        },
        series: [
          {
            data: chartData.privateCounts
          },
          {
            data: chartData.groupCounts
          }
        ]
      });
    }
  } catch (error) {
    console.error('加载图表数据失败:', error);
  }
};

// 处理图表数据，填充缺失的日期
const processChartData = (privateData: any[], groupData: any[]) => {
  const dates: string[] = [];
  const privateCounts: number[] = [];
  const groupCounts: number[] = [];

  // 生成日期范围
  const today = new Date();
  for (let i = selectedDays.value - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    dates.push(dateStr);

    // 查找对应日期的私聊数据
    const privateDayData = privateData.find(item => item.date === dateStr);
    privateCounts.push(privateDayData ? parseInt(privateDayData.count) : 0);

    // 查找对应日期的群聊数据
    const groupDayData = groupData.find(item => item.date === dateStr);
    groupCounts.push(groupDayData ? parseInt(groupDayData.count) : 0);
  }

  return { dates, privateCounts, groupCounts };
};

// 加载用户注册统计图表数据
const loadRegistrationChartData = async () => {
  try {
    const response = await getDailyRegistrationCount(registrationSelectedDays.value);
    const data = response.data;

    // 处理数据，确保所有日期都有数据
    const chartData = processRegistrationChartData(data);

    // 更新图表
    if (registrationChartInstance) {
      registrationChartInstance.setOption({
        xAxis: {
          data: chartData.dates
        },
        series: [{
          data: chartData.counts
        }]
      });
    }
  } catch (error) {
    console.error('加载用户注册统计图表数据失败:', error);
  }
};

// 处理用户注册统计图表数据，填充缺失的日期
const processRegistrationChartData = (data: any[]) => {
  const dates: string[] = [];
  const counts: number[] = [];

  // 生成日期范围
  const today = new Date();
  for (let i = registrationSelectedDays.value - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    dates.push(dateStr);

    // 查找对应日期的数据
    const dayData = data.find(item => item.date === dateStr);
    counts.push(dayData ? parseInt(dayData.count) : 0);
  }

  return { dates, counts };
};

// 初始化图表
const initChart = async () => {
  await nextTick();
  if (chartContainer.value) {
    chartInstance = echarts.init(chartContainer.value);

    const option = {
      title: {
        text: '每日消息量趋势对比',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          let result = `${params[0].name}<br/>`;
          params.forEach((param: any) => {
            result += `${param.seriesName}: ${param.value}<br/>`;
          });
          return result;
        }
      },
      legend: {
        data: ['私聊消息', '群聊消息'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          formatter: (value: string) => {
            return value.substring(5); // 只显示月-日
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '消息量'
      },
      series: [
        {
          name: '私聊消息',
          type: 'line',
          data: [],
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        },
        {
          name: '群聊消息',
          type: 'line',
          data: [],
          smooth: true,
          itemStyle: {
            color: '#67C23A'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(103, 194, 58, 0.3)'
              }, {
                offset: 1, color: 'rgba(103, 194, 58, 0.1)'
              }]
            }
          }
        }
      ],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '15%',
        containLabel: true
      }
    };

    chartInstance.setOption(option);

    // 加载初始数据
    await loadChartData();
  }
};

onMounted(() => {
  loadTotalUserCount();
  loadTotalGroupCount();
  loadActiveUserStats();
  loadTodayCheckinCount();
  loadTodayMsgUserCount();
  initChart();
  initRegistrationChart();
  initCheckinChart();
  initMsgUserChart();
});

// 初始化用户注册统计图表
const initRegistrationChart = async () => {
  await nextTick();
  if (registrationChartContainer.value) {
    registrationChartInstance = echarts.init(registrationChartContainer.value);

    const option = {
      title: {
        text: '每日用户注册趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const data = params[0];
          return `${data.name}<br/>注册用户: ${data.value}`;
        }
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          formatter: (value: string) => {
            return value.substring(5); // 只显示月-日
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '注册用户数'
      },
      series: [{
        name: '注册用户',
        type: 'line',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(230, 162, 60, 0.3)'
            }, {
              offset: 1, color: 'rgba(230, 162, 60, 0.1)'
            }]
          }
        }
      }],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      }
    };

    registrationChartInstance.setOption(option);

    // 加载初始数据
    await loadRegistrationChartData();
  }
};

// 通用：构建单折线图配置
const buildSingleLineOption = (title: string, yName: string, seriesName: string, color: string, rgb: string) => ({
  title: { text: title, left: 'center' },
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0];
      return `${data.name}<br/>${seriesName}: ${data.value}`;
    }
  },
  xAxis: {
    type: 'category',
    data: [] as string[],
    axisLabel: { formatter: (value: string) => value.substring(5) }
  },
  yAxis: { type: 'value', name: yName },
  series: [{
    name: seriesName,
    type: 'line',
    data: [] as number[],
    smooth: true,
    itemStyle: { color },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: `rgba(${rgb}, 0.3)` },
          { offset: 1, color: `rgba(${rgb}, 0.1)` }
        ]
      }
    }
  }],
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
});

// 加载每日签到用户数图表数据
const loadCheckinChartData = async () => {
  try {
    const response = await getDailyCheckinUserCount(checkinSelectedDays.value);
    const { dates, counts } = buildDailySeries(response.data, checkinSelectedDays.value);
    checkinChartInstance?.setOption({ xAxis: { data: dates }, series: [{ data: counts }] });
  } catch (error) {
    console.error('加载每日签到用户数图表数据失败:', error);
  }
};

// 初始化每日签到用户数图表
const initCheckinChart = async () => {
  await nextTick();
  if (checkinChartContainer.value) {
    checkinChartInstance = echarts.init(checkinChartContainer.value);
    checkinChartInstance.setOption(
      buildSingleLineOption('每日签到用户数趋势', '签到用户数', '签到用户', '#13C2C2', '19, 194, 194')
    );
    await loadCheckinChartData();
  }
};

// 加载每日发消息用户数图表数据
const loadMsgUserChartData = async () => {
  try {
    const response = await getDailyMessageUserCount(msgUserSelectedDays.value);
    const { dates, counts } = buildDailySeries(response.data, msgUserSelectedDays.value);
    msgUserChartInstance?.setOption({ xAxis: { data: dates }, series: [{ data: counts }] });
  } catch (error) {
    console.error('加载每日发消息用户数图表数据失败:', error);
  }
};

// 初始化每日发消息用户数图表
const initMsgUserChart = async () => {
  await nextTick();
  if (msgUserChartContainer.value) {
    msgUserChartInstance = echarts.init(msgUserChartContainer.value);
    msgUserChartInstance.setOption(
      buildSingleLineOption('每日发消息用户数趋势', '发消息用户数', '发消息用户', '#722ED1', '114, 46, 209')
    );
    await loadMsgUserChartData();
  }
};
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

// 统计卡片样式
.statistics-card {
  border-radius: 8px;
  border: 1px solid #f0f2f5;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.statistics-item {
  display: flex;
  align-items: center;

  .statistics-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    color: white;
  }

  .statistics-content {
    flex: 1;

    .statistics-title {
      font-size: 13px;
      color: #8c8c8c;
      margin-bottom: 4px;
    }

    .statistics-value {
      font-size: 24px;
      font-weight: 600;
      color: #262626;
      line-height: 1;
    }
  }
}

// 图表区域
.chart-section {
  margin-top: 24px;

  .el-card {
    border-radius: 8px;
    border: 1px solid #f0f2f5;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }
}

// 卡片头部样式优化
:deep(.el-card__header) {
  border-bottom: 1px solid #f0f2f5;
  padding: 12px 16px;

  span {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }
}
</style>

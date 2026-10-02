<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="群名" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入群名字" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="群主" prop="ownerIds">
              <im-user-select v-model="queryParams.ownerId"></im-user-select>
            </el-form-item>
            <el-form-item label="创建时间" prop="createdTime">
              <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" type="daterange"
                range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button type="warning" plain icon="Download" @click="handleExport"
                v-hasPermi="['im:group:export']">导出</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="groupList" @selection-change="handleSelectionChange">
        <el-table-column label="群名" align="center" prop="name" />
        <el-table-column label="群头像" align="center" prop="headImage" width="100">
          <template #default="scope">
            <image-preview v-if="scope.row.headImageThumb" :src="scope.row.headImageThumb"
              :full-src="scope.row.headImage" :width="40" :height="40" />
          </template>
        </el-table-column>
        <el-table-column label="群主" align="center" prop="ownerUserName" />
        <el-table-column label="成员数量" align="center">
          <template #default="scope">
            <span>{{ scope.row.memberCount }}</span>
            <el-tag v-if="scope.row.customMemberCount && scope.row.customMemberCount > 0" size="small" type="warning" style="margin-left: 6px;">
              虚拟: {{ scope.row.customMemberCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createdTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createdTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="是否解散" align="center" prop="dissolve">
          <template #default="scope">
            <dict-tag :options="im_bool" :value="scope.row.dissolve" />
          </template>
        </el-table-column>
        <el-table-column label="是否封禁" align="center" prop="isBanned">
          <template #default="scope">
            <dict-tag :options="im_bool" :value="scope.row.isBanned" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" v-hasPermi="['im:group:query']"
              @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.isBanned" link type="danger" v-hasPermi="['im:group:ban']"
              @click="handleUnban(scope.row)">解封</el-button>
            <el-button v-else link type="danger" v-hasPermi="['im:group:ban']"
              @click="handleBan(scope.row)">封禁</el-button>
            <el-button link type="primary" @click="handleShowMember(scope.row.id)">查看成员</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改群对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="800px" append-to-body>
      <el-form ref="groupFormRef" :model="form" label-width="110px">
        <el-form-item label="群头像" prop="headImage">
          <image-preview :src="form.headImageThumb" :full-src="form.headImage" :width="50" :height="50" />
        </el-form-item>
        <el-form-item label="群名字" prop="name">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="群主" prop="ownerUserName">
          <el-input v-model="form.ownerUserName" disabled />
        </el-form-item>
        <el-form-item label="创建时间" prop="createdTime">
          <el-date-picker disabled clearable v-model="form.createdTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否已解散" prop="dissolve">
          <dict-tag :options="im_bool" :value="form.dissolve" />
        </el-form-item>
        <el-form-item label="是否被封禁" prop="isBanned">
          <dict-tag :options="im_bool" :value="form.isBanned" />
        </el-form-item>
        <el-form-item v-if="form.isBanned" label="被封禁原因" prop="reason">
          <el-input v-model="form.reason" disabled />
        </el-form-item>
        <el-form-item label="群聊人数设置" prop="customMemberCount">
          <el-input-number v-model="form.customMemberCount" :min="0" :max="999999" placeholder="不设置则展示真实人数" style="width: 200px;" />
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">设置为0或留空表示展示真实群人数，设置大于0则在双端展示该虚拟人数</span>
        </el-form-item>
        <el-form-item v-if="form.notice" label="群公告" prop="notice">
          <el-input type="textarea" v-model="form.notice" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-drawer v-model="memberVisible" title="成员列表" :size="900" :close-on-press-escape="false"
      :close-on-click-modal="true">
      <member ref="memberRef"></member>
    </el-drawer>
  </div>
</template>

<script setup name="Group" lang="ts">
import { listGroup, getGroup, ban, unban, updateCustomMemberCount } from '@/api/im/group';
import { GroupVO, GroupQuery, GroupForm } from '@/api/im/group/types';
import member from './member.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const groupList = ref<GroupVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const memberVisible = ref(false);
const dateRange = ref<[DateModelType, DateModelType]>(['', '']);
const queryFormRef = ref<ElFormInstance>();
const groupFormRef = ref<ElFormInstance>();
const memberRef = ref();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: GroupForm = {
  id: undefined,
  name: undefined,
  ownerId: undefined,
  ownerUserName: undefined,
  headImage: undefined,
  headImageThumb: undefined,
  notice: undefined,
  dissolve: undefined,
  createdTime: undefined,
  isBanned: undefined,
  reason: undefined,
  customMemberCount: undefined
}
const data = reactive<PageData<GroupForm, GroupQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    ownerId: undefined,
    headImage: undefined,
    headImageThumb: undefined,
    notice: undefined,
    dissolve: undefined,
    createdTime: undefined,
    isBanned: undefined,
    reason: undefined,
    params: {
    }
  },
  rules: {

  }
});

const { queryParams, form } = toRefs(data);
const { im_bool } = toRefs<any>(proxy?.useDict('im_bool'));

/** 查询群列表 */
const getList = async () => {
  loading.value = true;
  const res = await listGroup(proxy?.addDateRange(queryParams.value, dateRange.value));
  groupList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}



/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  groupFormRef.value?.resetFields();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', ''];
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: GroupVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 详情按钮操作 */
const handleDetail = async (row?: GroupVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getGroup(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "群聊详情";
}

const handleBan = (group: any) => {
  ElMessageBox.prompt('封禁原因:', '确定对该群组进行封禁？', {
    inputPattern: /\S/,
    inputErrorMessage: '请输入封禁原因',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    const data = { id: group.id, reason: value }
    ban(data).then(() => {
      group.isBanned = true;
      ElMessage.success(`群组'${group.name}'已被封禁`);
    })
  })
}

const handleUnban = (group: any) => {
  ElMessageBox.confirm('确定解除该群组的封禁状态？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    const data = { id: group.id }
    unban(data).then(() => {
      group.isBanned = false;
      ElMessage.success(`群组'${group.name}'解封成功`);
    })
  })
}

const handleShowMember = (id: number) => {
  memberVisible.value = true;
  nextTick(() => memberRef.value.init(id));
}

/** 提交按钮 */
const submitForm = async () => {
  if (form.value.id) {
    buttonLoading.value = true;
    try {
      await updateCustomMemberCount({
        id: form.value.id,
        customMemberCount: form.value.customMemberCount || 0
      });
      ElMessage.success("群聊人数设置成功");
      dialog.visible = false;
      getList();
    } finally {
      buttonLoading.value = false;
    }
  } else {
    dialog.visible = false;
  }
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download('im/group/export', {
    ...queryParams.value
  }, `group_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList();
});
</script>

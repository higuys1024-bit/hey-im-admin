<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter"
      :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="queryParams.userName" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="queryParams.nickName" placeholder="请输入用户昵称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="邀请码" prop="inviteCode">
              <el-input v-model="queryParams.inviteCode" placeholder="请输入邀请码" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              <el-button type="warning" plain icon="Download" @click="handleExport"
                v-hasPermi="['im:user:export']">导出</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
        <el-table-column label="用户名" align="center" prop="userName" />
        <el-table-column label="用户昵称" align="center" prop="nickName" />
        <el-table-column label="邀请码" align="center" prop="inviteCode" />
        <el-table-column label="用户头像" align="center" prop="headImageThumb" width="100">
          <template #default="scope">
            <image-preview :src="scope.row.headImageThumb" :full-src="scope.row.headImage" :width="50" :height="50" />
          </template>
        </el-table-column>
        <el-table-column label="性别" align="center" prop="sex">
          <template #default="scope">
            <dict-tag :options="sys_user_sex" :value="scope.row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="是否被封禁" align="center" prop="isBanned">
          <template #default="scope">
            <dict-tag :options="im_bool" :value="scope.row.isBanned" />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" align="center" prop="createdTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createdTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="地址" align="center" prop="location" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.location || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" align="center" prop="lastLoginTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.lastLoginTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" v-hasPermi="['im:user:query']"
              @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="success" v-hasPermi="['im:user:query']"
              @click="handleSubordinates(scope.row)">下级查询</el-button>
            <el-button v-if="scope.row.isBanned" link type="danger" v-hasPermi="['im:user:ban']"
              @click="unbanHandle(scope.row)">解封</el-button>
            <el-button v-else link type="danger" v-hasPermi="['im:user:ban']"
              @click="banHandle(scope.row)">封禁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改用户对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="800px" append-to-body>
      <el-form ref="userFormRef" :model="form" :rules="rules" label-width="100px" disabled>
        <el-form-item label="用户头像" prop="headImage">
          <image-preview v-if="form.headImageThumb" :src="form.headImageThumb" :full-src="form.headImage"
            :width="100" :height="100" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <dict-tag :options="sys_user_sex" :value="form.sex" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="form.nickName"  />
        </el-form-item>
        <el-form-item label="个性签名" prop="signature">
          <el-input v-model="form.signature" />
        </el-form-item>
        <el-form-item label="邀请码" prop="inviteCode">
          <el-input v-model="form.inviteCode" />
        </el-form-item>
        <el-form-item label="上级信息" prop="inviterUserName">
          <el-input :model-value="inviterDisplay" placeholder="无上级（直接注册或根邀请码）" />
        </el-form-item>
        <el-form-item label="最后登录时间" prop="lastLoginTime">
          <el-date-picker clearable v-model="form.lastLoginTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="注册时间" prop="createdTime">
          <el-date-picker clearable v-model="form.createdTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="地址" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="是否被封禁" prop="isBanned">
          <dict-tag :options="im_bool" :value="form.isBanned" />
        </el-form-item>
        <el-form-item v-if="form.isBanned" label="被封禁原因" prop="reason">
          <el-input v-model="form.reason"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="warning" plain v-hasPermi="['im:user:resetPwd']"
            @click="resetPwdHandle">重置登录密码</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 下级查询对话框 -->
    <el-dialog :title="subDialog.title" v-model="subDialog.visible" width="800px" append-to-body>
      <el-table v-loading="subLoading" :data="subList" max-height="500">
        <el-table-column label="账号" align="center" prop="userName" />
        <el-table-column label="姓名" align="center" prop="nickName" />
        <el-table-column label="注册时间" align="center" prop="createdTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createdTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下级人数" align="center" prop="subordinateCount" width="100" />
      </el-table>
      <el-empty v-if="!subLoading && subList.length === 0" description="该用户暂无下级" />
    </el-dialog>
  </div>
</template>

<script setup name="User" lang="ts">
import { listUser, getUser, ban, unban, resetUserPwd, getSubordinates } from '@/api/im/user';
import { UserVO, UserQuery, UserForm, SubordinateVO } from '@/api/im/user/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const userList = ref<UserVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const userFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: UserForm = {
  id: undefined,
  userName: undefined,
  nickName: undefined,
  headImage: undefined,
  headImageThumb: undefined,
  password: undefined,
  sex: undefined,
  signature: undefined,
  inviteCode: undefined,
  inviterUserName: undefined,
  inviterNickName: undefined,
  lastLoginTime: undefined,
  location: undefined,
  createdTime: undefined,
  type: undefined,
  isBanned: undefined,
  reason: undefined
}
const data = reactive<PageData<UserForm, UserQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    nickName: undefined,
    inviteCode: undefined,
    params: {
    }
  },
  rules: {}
});
const { queryParams, form, rules } = toRefs(data);

const { im_bool } = toRefs<any>(proxy?.useDict('im_bool'));
const { sys_user_sex } = toRefs<any>(proxy?.useDict('sys_user_sex'));

// 上级信息展示：账号(姓名)
const inviterDisplay = computed(() => {
  if (!form.value.inviterUserName) return '';
  return `${form.value.inviterUserName}（${form.value.inviterNickName || '未设置昵称'}）`;
});

// 下级查询对话框
const subDialog = reactive<DialogOption>({ visible: false, title: '' });
const subLoading = ref(false);
const subList = ref<SubordinateVO[]>([]);

/** 查询用户列表 */
const getList = async () => {
  loading.value = true;
  const res = await listUser(queryParams.value);
  userList.value = res.rows;
  total.value = res.total;
  loading.value = false;
  console.log("getList")
}
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
  console.log("handleQuery")
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
  console.log("handleQuery")
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: UserVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  console.log("handleSelectionChange")
}

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  userFormRef.value?.resetFields();
  console.log("reset")
}

/** 修改按钮操作 */
const handleDetail = async (row?: UserVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getUser(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "用户信息";
}

/** 提交按钮 */
const submitForm = () => {
  dialog.visible = false;
}

/** 下级查询 */
const handleSubordinates = async (row: UserVO) => {
  subDialog.visible = true;
  subDialog.title = `「${row.nickName || row.userName}」的下级用户`;
  subList.value = [];
  subLoading.value = true;
  try {
    const res = await getSubordinates(row.id);
    subList.value = res.data || [];
  } finally {
    subLoading.value = false;
  }
}

/** 重置用户登录密码 */
const resetPwdHandle = () => {
  ElMessageBox.prompt(`请输入用户'${form.value.userName}'的新登录密码:`, '重置登录密码', {
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度必须在6-20位之间',
    inputType: 'password',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    resetUserPwd({ id: form.value.id, password: value }).then(() => {
      ElMessage.success(`用户'${form.value.userName}'登录密码重置成功`);
    })
  })
}

const banHandle = (user: any) => {
  ElMessageBox.prompt('封禁原因:', '确定对该用户进行封禁？', {
    inputPattern: /\S/,
    inputErrorMessage: '请输入封禁原因',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    const data = { id: user.id, reason: value }
    ban(data).then(() => {
      user.isBanned = true
      ElMessage.success(`用户'${user.userName}'已被封禁`);
    })
  })
}

const unbanHandle = (user: any) => {
  ElMessageBox.confirm('确定解除该用户的封禁状态？？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    const data = { id: user.id }
    unban(data).then(() => {
      user.isBanned = false
      ElMessage.success(`用户'${user.userName}'解锁成功`);
    })

  })
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download('im/user/export', {
    ...queryParams.value
  }, `user_${new Date().getTime()}.xlsx`)
}


onMounted(() => {
  getList();
});
</script>

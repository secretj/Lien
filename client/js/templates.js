// 인증 체크
if (!Auth.requireAuth()) {
    // requireAuth에서 리다이렉트 처리
}

// 사용자 정보 표시
const user = Auth.getUser();
if (user) {
    document.getElementById('userInfo').textContent = user.name;
}

// 템플릿 목록 로드
async function loadTemplates()
{
    const grid = document.getElementById('templatesGrid');
    const emptyState = document.getElementById('emptyState');

    try {
        const response = await TemplatesAPI.getTemplates();
        const templates = response.content || [];

        if (templates.length === 0) {
            grid.style.display = 'none';
            emptyState.style.display = 'block';
            return;
        }

        grid.innerHTML = templates.map(template => `
            <div class="template-card" onclick="viewTemplate(${template.id})">
                <h3>${template.title}</h3>
                <p class="destination">📍 ${template.destination}</p>
                <div class="dates">
                    📅 ${formatDate(template.startDate)} - ${formatDate(template.endDate)}
                </div>
                <div class="info-row">
                    <span>${template.totalDays}일</span>
                    <span>${formatDateTime(template.createdAt)}</span>
                </div>
            </div>
        `).join('');

        grid.style.display = 'grid';
        emptyState.style.display = 'none';

    } catch (error) {
        console.error('템플릿 로드 실패:', error);
        grid.innerHTML = `
            <div class="alert alert-error">
                템플릿을 불러오는데 실패했습니다: ${error.message}
            </div>
        `;
    }
}

function formatDate(dateStr)
{
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function formatDateTime(dateStr)
{
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric'
    });
}

function viewTemplate(templateId)
{
    window.location.href = `template-detail.html?id=${templateId}`;
}

// 모달 관리
function openCreateModal()
{
    document.getElementById('createModal').classList.add('show');
}

function closeCreateModal()
{
    document.getElementById('createModal').classList.remove('show');
    document.getElementById('createTemplateForm').reset();
}

// 템플릿 생성
document.getElementById('createTemplateForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = {
        title: document.getElementById('title').value,
        destination: document.getElementById('destination').value,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        totalDays: parseInt(document.getElementById('totalDays').value),
        accommodation: document.getElementById('accommodation').value || null,
        transportation: document.getElementById('transportation').value || null
    };

    try {
        await TemplatesAPI.createTemplate(formData);
        closeCreateModal();
        loadTemplates();
        alert('템플릿이 생성되었습니다!');
    } catch (error) {
        alert('템플릿 생성 실패: ' + error.message);
    }
});

function handleLogout()
{
    if (confirm('로그아웃 하시겠습니까?')) {
        Auth.logout();
    }
}

// 페이지 로드 시 템플릿 목록 불러오기
loadTemplates();


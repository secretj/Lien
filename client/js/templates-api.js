class TemplatesAPI
{
    static async getTemplates()
    {
        const token = Auth.getToken();
        return API.request('/templates', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }

    static async createTemplate(data)
    {
        const token = Auth.getToken();
        return API.request('/templates', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });
    }

    static async getTemplate(templateId)
    {
        const token = Auth.getToken();
        return API.request(`/templates/${templateId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }

    static async deleteTemplate(templateId)
    {
        const token = Auth.getToken();
        return API.request(`/templates/${templateId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
}


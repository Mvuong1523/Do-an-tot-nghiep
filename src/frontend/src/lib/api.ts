export const API_BASE_URL = 'http://localhost:8080/api'
export const apiGet = async (endpoint: string, token?: string) => {
    //tạo header object
    const headers: HeadersInit = {
        'Content-Type': 'application/json'
    }

    // nếu có token, thêm vào Authorization header
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    // Gọi fetch với method GET
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'GET', headers,
    })
    //  Retủn kết quả dạng json
    return response.json()
}
export const apiPost = async (endpoint: string, data?: any, token?: string) => {
    // tao header object
    const headers: HeadersInit = {
        'Content-Type': 'application/json'
    }

    //neu co token, them vao Authorization header
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    //fetch voi method post
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST', headers, body: JSON.stringify(data),
    })
    return response.json()
}
export const apiPut = async (endpoint: string, data?: any, token?: string) => {

    const headers: HeadersInit = {
        'Content-Type': 'application/json'
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'PUT',
        headers,
        body: JSON.stringify(data),
    }
    )
    return response.json()
}

export const apiDelete = async (endpoint: string, token?: string) => {

    const headers: HeadersInit = {
        'Content-Type': 'application/json'
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'DELETE',
        headers,
    })
    return response.json()
}



import { useEffect, useState } from "react"

/**
 * BÀI TẬP: STATE VÀ USEEFFECT
 * 
 * Làm lần lượt từng bài, comment code bài cũ trước khi làm bài mới
 */

export default function ExercisePage() {

    // ============================================
    // BÀI 1: ĐƠN GIẢN - Counter với nút bấm
    // ============================================
    // YÊU CẦU:
    // 1. Tạo state "count" bắt đầu từ 0
    // 2. Tạo 3 nút: "+1", "-1", "Reset"
    // 3. Hiển thị giá trị count lên màn hình
    // 
    // GỢI Ý:
    // - Dùng useState(0)
    // - onClick={() => setCount(...)}

    // ✍️ CODE CỦA BẠN Ở ĐÂY:
    // const [count, setCount] = useState<number>(0)

    // const tang = () => setCount(count + 1)
    // const giam = () => setCount(count - 1)
    // const Reset = () => setCount(0)
    // return (
    //     <div>
    //         <h1> {count} </h1>
    //         <button onClick={() => tang()}> tăng 1 giây</button>
    //         <br />
    //         <button onClick={() => giam()}>giảm 1 giây</button>
    //         <br />
    //         <button onClick={() => Reset()}>về 0</button>
    //     </div>

    // )




    // ============================================
    // BÀI 2: TRUNG BÌNH - Toggle Dark Mode
    // ============================================
    // YÊU CẦU:
    // 1. Tạo state "isDark" (true/false)
    // 2. Nút "Toggle Theme" để đổi sáng/tối
    // 3. Dùng useEffect để thay đổi document.title
    //    - Nếu isDark: "Dark Mode"
    //    - Nếu không: "Light Mode"
    // 4. Thay đổi màu nền:
    //    - Dark: nền đen, chữ trắng
    //    - Light: nền trắng, chữ đen
    //
    // GỢI Ý:
    // - useState(false)
    // - useEffect(() => { document.title = ... }, [isDark])
    // - style={{ background: isDark ? 'black' : 'white' }}

    // ✍️ CODE CỦA BẠN Ở ĐÂY:
    // const [isDark, setIsDark] = useState<boolean>(false)
    // useEffect(() => {
    //     document.title = isDark ? 'Dark Mode' : 'Light Mode'
    // }, [isDark])
    // const doi = () => {
    //     if (isDark) { setIsDark(false) }
    //     else { setIsDark(true) }
    // }
    // return (
    //     <div style={{ background: isDark ? 'black' : 'white', color: isDark ? 'white' : 'black' }}>
    //         <h1>đổi màu nền, chua hiểu sao phair dùng useState</h1>
    //         <button onClick={() => doi()}>bam de doi</button>
    //     </div>
    // )





    // ============================================
    // BÀI 3: TRUNG BÌNH - Real-time Clock
    // ============================================
    // YÊU CẦU:
    // 1. Hiển thị giờ:phút:giây hiện tại
    // 2. Cập nhật mỗi giây
    // 3. Có nút "Start" và "Stop" để bật/tắt đồng hồ
    // 4. Nhớ cleanup khi component unmount!
    //
    // GỢI Ý:
    // - useState(new Date())
    // - useState(true) // isRunning
    // - useEffect với setInterval
    // - time.toLocaleTimeString()
    // - return () => clearInterval(id)

    // ✍️ CODE CỦA BẠN Ở ĐÂY:
    // const [time, setTime] = useState(new Date())
    // const [isRunning, setIsRunning] = useState<boolean>(true)
    // useEffect(() => {
    //     if (!isRunning) return
    //     const id = setInterval(() => {
    //         setTime(new Date())
    //     }, 1000)
    //     return () => clearInterval(id)
    // }, [isRunning])
    // return (
    //     <div>
    //         <h1>Real-time clock</h1>
    //         <br />
    //         <h2>{time.toLocaleTimeString()}</h2>
    //         <button onClick={() => setIsRunning(!isRunning)}>{isRunning ? 'start' : 'stop'}</button>
    //     </div>
    // )




    // ============================================
    // BÀI 4: KHÓ - Countdown Timer
    // ============================================
    // YÊU CẦU:
    // 1. Input để nhập số giây (VD: 60)
    // 2. Nút "Start" để bắt đầu đếm ngược
    // 3. Hiển thị thời gian còn lại (MM:SS)
    // 4. Khi về 0 → alert("Time's up!")
    // 5. Nút "Pause" và "Reset"
    //
    // GỢI Ý:
    // - useState(60) // seconds
    // - useState(false) // isRunning
    // - useEffect chạy khi isRunning thay đổi
    // - Khi seconds === 0 → alert và dừng
    // - Math.floor(seconds / 60) : seconds % 60

    // ✍️ CODE CỦA BẠN Ở ĐÂY:
    // const [time, setTime] = useState<number>(60)
    // const [isRunning, setIsRunning] = useState<boolean>(false)
    // const [dem, setDem] = useState<number>(60)
    // useEffect(() => {
    //     if (!isRunning) return
    //     const id = setInterval(() => {
    //         setTime((p) => p - 1)
    //     }, 1000)
    //     return () => clearInterval(id)

    // }, [isRunning])
    // const phut = Math.floor(time / 60)
    // const giay = Math.floor(time % 60)
    // const lai = () => setTime(dem)
    // return (
    //     <div>
    //         <input
    //             type="Text"
    //             onChange={(e) => setDem(Number(e.target.value))}
    //         />
    //         <h2>{`${phut}: ${giay < 10 ? '0' : ''}${giay}`}</h2>
    //         <h2>{time === 0 ? 'het gio' : 'dang chay'}</h2>
    //         <button onClick={() => setIsRunning(!isRunning)}> {isRunning ? 'dừng' : 'tiếp tục'}</button>
    //         <br />
    //         <button onClick={() => lai()}>reset</button>
    //     </div>
    // )





    // ============================================
    // BÀI 5: KHÓ - Fetch GitHub User
    // ============================================
    // YÊU CẦU:
    // 1. Input để nhập GitHub username
    // 2. Nút "Search" để tìm user
    // 3. Dùng useEffect + fetch để gọi API:
    //    https://api.github.com/users/{username}
    // 4. Hiển thị:
    //    - Avatar
    //    - Name
    //    - Bio
    //    - Followers
    // 5. Hiển thị "Loading..." khi đang fetch
    // 6. Hiển thị lỗi nếu user không tồn tại
    //
    // GỢI Ý:
    // - useState('') // username
    // - useState(null) // userData
    // - useState(false) // loading
    // - useState('') // error
    // - useEffect(() => { fetch(...) }, [searchTrigger])

    // ✍️ CODE CỦA BẠN Ở ĐÂY:
    const [userName, setUserName] = useState<string>('')
    const [userData, setUserData] = useState<any>(null)
    const [loading, setLoading] = useState<boolean>(false)
    const [error, setError] = useState<string>('')
    const [trigger, setTrigger] = useState<number>(0)
    useEffect(() => {
        if (!userName || trigger === 0) return
        setLoading(true)
        setError('')
        fetch(`http://api.github.com/users/${userName}`).then(Response => {
            if (!Response.ok) throw new Error('Không tìm thấy user')
            return Response.json()
        }).then(data => {
            setUserData(data)
            setLoading(false)
        }).catch(err => {
            setError(err.message)
            setLoading(false)
        })
    }, [trigger])
    return (
        <div>
            <input
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
            />
            <button onClick={() => setTrigger(prev => prev + 1)}>tìm kiếm</button>
            {loading && <p>dang tai</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            {userData && (
                <div>
                    <img src={userData.avatar_url} width="100" />
                    <h2>{userData.login}</h2>
                    <p>{userData.id}</p>
                    <p>{userData.type}</p>
                </div>
            )}
        </div>
    )



    // ============================================
    // BÀI 6: NÂNG CAO - Todo List với LocalStorage
    // ============================================
    // YÊU CẦU:
    // 1. Input để thêm todo mới
    // 2. Hiển thị danh sách todos
    // 3. Mỗi todo có:
    //    - Checkbox để đánh dấu hoàn thành
    //    - Nút xóa
    // 4. Lưu todos vào localStorage
    // 5. Khi reload trang → load todos từ localStorage
    // 6. Hiển thị số lượng todo chưa hoàn thành
    //
    // GỢI Ý:
    // - useState([]) // todos: { id, text, completed }
    // - useEffect(() => { 
    //     const saved = localStorage.getItem('todos')
    //   }, [])
    // - useEffect(() => {
    //     localStorage.setItem('todos', JSON.stringify(todos))
    //   }, [todos])
    // - Date.now() để tạo unique id

    // ✍️ CODE CỦA BẠN Ở ĐÂY:







}

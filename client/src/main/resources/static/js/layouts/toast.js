function showToast(message) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.classList.add("show");

    // 지정한 시간 후에 사라짐
    setTimeout(() => {
        toast.classList.remove("show");
    }, 5000);
}

export { showToast };
// CKEditor 연결
let reviewEditor;

ClassicEditor.create(document.querySelector('#content'), {
    extraPlugins: [MyCustomUploadAdapterPlugin],
    language: 'ko'
  })
  .then(editor => {
    reviewEditor = editor;
  })
  .catch(error => {
    console.log(error);
  });

function MyCustomUploadAdapterPlugin(editor) {
  editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
    return new UploadAdapter(loader);
  }
}

// CKEditor 5: 이미지 파일 업로드 어댑터
class UploadAdapter {
  constructor(loader) {
    this.loader = loader;
  }

  upload() {
    return this.loader.file.then( file => new Promise(((resolve, reject) => {
      this._initRequest();
      this._initListeners( resolve, reject, file );
      this._sendRequest( file );
    })))
  }

  // 서버 요청 경로
  _initRequest() {
    const xhr = this.xhr = new XMLHttpRequest();
    xhr.open('POST', location.protocol + "//" + location.host + '/reviews/upload_temp_image', true);
    xhr.responseType = 'json';
  }

  _initListeners(resolve, reject, file) {
    const xhr = this.xhr;
    const loader = this.loader;
    const genericErrorText = '파일을 업로드 할 수 없습니다.'

    xhr.addEventListener('error', () => {reject(genericErrorText)})
    xhr.addEventListener('abort', () => reject())
    xhr.addEventListener('load', () => {
      const response = xhr.response
      if(!response || response.error) {
        return reject( response && response.error ? response.error.message : genericErrorText );
      }

      resolve({
        default: response.url
      })
    })
  }

  _sendRequest(file) {
    const data = new FormData()
    data.append('upload',file)
    this.xhr.send(data)
  }
}

export {reviewEditor}
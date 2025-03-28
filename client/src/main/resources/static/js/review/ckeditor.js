import { accessTokenUtils } from '/js/common.js';

// CKEditor 연결
let memberId = accessTokenUtils.getMemberId();
let reviewEditor;
let tempImageFileNames = [];

ClassicEditor.create(document.querySelector('#content'), {
    extraPlugins: [MyCustomUploadAdapterPlugin],
    language: 'ko'
})
.then(editor => {
    reviewEditor = editor;
    editor.model.schema.extend('imageInline', {
        allowAttributes: ['data-image-index', 'data-image-name']
    });

    // (데이터 모델 → 뷰) data-image-number 속성이 모델에서 뷰로 변환될 때 유지되도록 설정
    // downcast: 모델에서 <img> 태그로 변환할 때 data-image-index, data-image-name 속성 유지
    editor.conversion.for('downcast').add(dispatcher => {
        dispatcher.on('attribute:data-image-index:imageInline', (evt, data, conversionApi) => {
            const { writer } = conversionApi;
            const viewElement = conversionApi.mapper.toViewElement(data.item);

            if (viewElement) {
                // <span> 내부의 <img>
                const imgElement = viewElement.getChild(0);
                if (imgElement) {
                    writer.setAttribute('data-image-index', data.attributeNewValue, imgElement);
                }
            }
        });

        dispatcher.on('attribute:data-image-name:imageInline', (evt, data, conversionApi) => {
            const { writer } = conversionApi;
            const viewElement = conversionApi.mapper.toViewElement(data.item);

            if (viewElement) {
                // <span> 내부의 <img>
                const imgElement = viewElement.getChild(0);
                if (imgElement) {
                    writer.setAttribute('data-image-name', data.attributeNewValue, imgElement);
                }
            }
        });
    });

    // (뷰 → 데이터 모델) data-image-index, data-image-name 속성이 뷰에서 모델로 변환될 때 유지되도록 설정
    // upcast: <img> 태그에서 data-image-index, data-image-name 속성을 모델로 변환
    editor.conversion.for('upcast').attributeToAttribute({
        view: 'data-image-index',
        model: 'data-image-index'
    });

    editor.conversion.for('upcast').attributeToAttribute({
        view: 'data-image-name',
        model: 'data-image-name'
    });

    // 뷰의 속성과 모델의 속성에 data-image-index, data-image-name 를 추가
    editor.conversion.attributeToAttribute({
        model: 'data-image-index',
        view: 'data-image-index'
    });

    editor.conversion.attributeToAttribute({
        model: 'data-image-name',
        view: 'data-image-name'
    });
})
.catch(error => {
    console.log(error);
});

function MyCustomUploadAdapterPlugin(editor) {
    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new UploadAdapter(loader, editor);
    }
}

// CKEditor 5: 이미지 파일 업로드 어댑터
class UploadAdapter {
    constructor(loader, editor) {
        this.loader = loader;
        this.editor = editor;
    }

    upload() {
        return this.loader.file.then(file => new Promise(((resolve, reject) => {
            this._initRequest();
            this._initListeners(resolve, reject, file);
            this._sendRequest(file);
        })))
    }

    // 에디터에서 글 작성 중 이미지를 올리면 해당 임시 이미지를 서버에 저장하는 요청 경로
    _initRequest() {
        const xhr = this.xhr = new XMLHttpRequest();

        // xhr.open('POST', location.protocol + '//' + location.host + 'reviews/content-temp-image', true);
        xhr.open('POST', 'http://localhost:8081/api/v1/reviews/content-images/temp/' + memberId, true);
        xhr.setRequestHeader('Authorization', 'Bearer ' + accessTokenUtils.getAccessToken());

        // 응답 타입
        xhr.responseType = 'json';
    }

    _initListeners(resolve, reject, file) {
        const xhr = this.xhr;
        const loader = this.loader;
        const genericErrorText = '파일을 업로드 할 수 없습니다.'

        xhr.addEventListener('error', () => {reject(genericErrorText)});
        xhr.addEventListener('abort', () => reject());
        xhr.addEventListener('load', () => {
            // 서버에서 반환한 응답 본문
            // 본문 형식 → {originalFileName: 'lenna1.png', savedFileName: '8c513a83.png'}
            const response = xhr.response;
            // 리뷰 작성 중에 사용되는 파일 리스트에 담는다
            tempImageFileNames.push(response);

            // 이미지 인덱스
            // 리뷰 등록할 때 사용되고 있으며 리뷰 수정할 때 JS 코드를 참고하여 이미지 이름으로 처리해도 된다
            const imageIndex = tempImageFileNames.length - 1;
            // 이미지 이름
            // 리뷰 수정할 때, imageIndex 로는 배열 개수로 인덱스를 구하여 오동작이 발생하여 이미지의 고유이름으로 처리
            const imageName = response.savedFileName;

            // <img> 태그에 이미지 번호 추가
            this.editor.model.change(writer => {
                // <img> 태그의 src 속성 변경
                const imageElement = writer.createElement('imageInline', {
                    'src': 'http://localhost:8081/api/v1/reviews/content-images/temp/' +
                            memberId + '/' + response.savedFileName,
                    'data-image-index': imageIndex,
                    'data-image-name': imageName
                });

                // <img> 태그에 속성을 추가하는 다른 방법
                // 위에서 'data-number': imageNumber' 제거하고 아래 코드를 작성할 수 있다
                // writer.setAttribute('data-image-number', imageNumber, imageElement);

                this.editor.model.insertContent(imageElement);
            });
            if (!response || response.error) {
                return reject(response && response.error ? response.error.message : genericErrorText);
            }
            resolve({default: response.url});
        });
    }

    // 업로드 파일 요청
    _sendRequest(file) {
        const data = new FormData();
        // 글 작성 중 서버로 전달되는 임시 이미지 파일이 'upload' 라는 이름으로 전달된다
        // 서버는 MultipartRequest 로 받는다 → MultipartFile tempImageFile = multipartRequest.getFile("upload");
        data.append('upload', file);
        this.xhr.send(data);
    }
}

export { reviewEditor, tempImageFileNames };
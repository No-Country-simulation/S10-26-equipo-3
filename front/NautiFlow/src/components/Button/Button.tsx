interface props{
    texto:string;
    type?:"submit" | 'reset' | 'button';
    onclick?: ()=>void;
}

export default function Button({texto,type,onclick}:props, ){

    return(
        <>
            <button type={type} onClick={onclick}  className="text-button-sm-size leading-button-sm-line-height font-button-sm w-full bg-bg-fill-primary rounded-full text-text-invert py-2 px-4 md:border-info cursor-pointer"   >{texto}</button>
        </>
    )
}
import Logo from "../../Logo"
export default function HeaderConfirmacion(){

    return(
        <header className="flex flex-row gap-3 items-center md:h-32">
                <Logo/>
                <p className='hidden md:block text-heading-lg-size leading-heading-lg-line-height font-heading-lg'>NautiFlow</p>
        </header>
    )

}